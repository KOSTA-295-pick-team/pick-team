package com.pickteam.controller.board;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pickteam.config.TestSecurityConfig;
import com.pickteam.dto.board.PostCreateDto;
import com.pickteam.dto.board.PostResponseDto;
import com.pickteam.exception.GlobalExceptionHandler;
import com.pickteam.service.board.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 게시글 컨트롤러 단위 테스트
 * @WebMvcTest를 사용하여 Presentation Layer만 테스트
 * Security 설정은 제외하고 Controller 로직만 검증
 */
@WebMvcTest(
        value = PostController.class,
        useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE, 
                classes = PostController.class
        )
)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.datasource.initialization-mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")  // 이러면 @Profile("!test")는 제외됨
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("게시글 목록 조회 시 정상적인 요청이면 200 OK와 함께 페이징된 게시글 목록을 반환한다")
    void getPosts_ValidRequest_Returns200OK() throws Exception {
        // given
        Long teamId = 1L;
        Long boardId = 10L;
        
        PostResponseDto post1 = createPostResponseDto(1L, "첫 번째 게시글", 100L, "홍길동");
        PostResponseDto post2 = createPostResponseDto(2L, "두 번째 게시글", 101L, "김철수");
        
        Page<PostResponseDto> expectedPage = new PageImpl<>(
                List.of(post1, post2), 
                PageRequest.of(0, 5), 
                2L
        );

        given(postService.getPosts(eq(boardId), any()))
                .willReturn(expectedPage);

        // when & then
        mockMvc.perform(get("/api/teams/{teamId}/posts", teamId)
                        .param("boardId", boardId.toString())
                        .param("page", "0")
                        .param("size", "5"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("첫 번째 게시글"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("게시글 생성 시 정상적인 요청이면 201 Created와 함께 생성된 게시글을 반환한다")
    void createPost_ValidRequest_Returns201Created() throws Exception {
        // given
        Long teamId = 1L;
        Long accountId = 100L;
        
        PostCreateDto request = new PostCreateDto();
        request.setTitle("새로운 게시글");
        request.setContent("게시글 내용입니다.");
        request.setBoardId(10L);

        PostResponseDto expectedResponse = createPostResponseDto(1L, "새로운 게시글", accountId, "홍길동");

        given(postService.createPost(any(PostCreateDto.class), eq(accountId)))
                .willReturn(expectedResponse);

        // when & then
        mockMvc.perform(post("/api/teams/{teamId}/posts", teamId)
                        .param("accountId", accountId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("새로운 게시글"))
                .andExpect(jsonPath("$.content").value("게시글 내용입니다."))
                .andExpect(jsonPath("$.authorName").value("홍길동"));

        verify(postService).createPost(any(PostCreateDto.class), eq(accountId));
    }

    @Test
    @DisplayName("게시글 생성 시 제목이 비어있으면 400 Bad Request를 반환한다")
    void createPost_EmptyTitle_Returns400BadRequest() throws Exception {
        // given
        Long teamId = 1L;
        Long accountId = 100L;
        
        PostCreateDto request = new PostCreateDto();
        request.setTitle("");  // 빈 제목
        request.setContent("게시글 내용입니다.");
        request.setBoardId(10L);

        // when & then
        mockMvc.perform(post("/api/teams/{teamId}/posts", teamId)
                        .param("accountId", accountId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("게시글 생성 시 내용이 비어있으면 400 Bad Request를 반환한다")
    void createPost_EmptyContent_Returns400BadRequest() throws Exception {
        // given
        Long teamId = 1L;
        Long accountId = 100L;
        
        PostCreateDto request = new PostCreateDto();
        request.setTitle("게시글 제목");
        request.setContent("");  // 빈 내용
        request.setBoardId(10L);

        // when & then
        mockMvc.perform(post("/api/teams/{teamId}/posts", teamId)
                        .param("accountId", accountId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("게시글 생성 시 게시판 ID가 null이면 400 Bad Request를 반환한다")
    void createPost_NullBoardId_Returns400BadRequest() throws Exception {
        // given
        Long teamId = 1L;
        Long accountId = 100L;
        
        PostCreateDto request = new PostCreateDto();
        request.setTitle("게시글 제목");
        request.setContent("게시글 내용입니다.");
        request.setBoardId(null);  // null 게시판 ID

        // when & then
        mockMvc.perform(post("/api/teams/{teamId}/posts", teamId)
                        .param("accountId", accountId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // 테스트 헬퍼 메서드
    private PostResponseDto createPostResponseDto(Long id, String title, Long authorId, String authorName) {
        PostResponseDto dto = new PostResponseDto();
        dto.setId(id);
        dto.setPostNo(1);
        dto.setTitle(title);
        dto.setContent("게시글 내용입니다.");
        dto.setAuthorId(authorId);
        dto.setAuthorName(authorName);
        dto.setBoardId(10L);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        dto.setCommentCount(0);
        return dto;
    }
}
