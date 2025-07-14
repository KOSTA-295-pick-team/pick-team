package com.pickteam.controller.board;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pickteam.config.TestSecurityConfig;
import com.pickteam.dto.board.CommentCreateDto;
import com.pickteam.dto.board.CommentResponseDto;
import com.pickteam.exception.GlobalExceptionHandler;
import com.pickteam.service.board.CommentService;
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
 * 댓글 컨트롤러 단위 테스트
 * @WebMvcTest를 사용하여 Presentation Layer만 테스트
 * Security 설정은 제외하고 Controller 로직만 검증
 */
@WebMvcTest(
        value = CommentController.class,
        useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = CommentController.class
        ),
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
        }
)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.datasource.initialization-mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")  // 이러면 @Profile("!test")는 제외됨
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("댓글 생성 시 정상적인 요청이면 200 OK와 함께 생성된 댓글을 반환한다")
    void createComment_ValidRequest_Returns200OK() throws Exception {
        // given
        Long postId = 1L;
        Long accountId = 100L;

        CommentCreateDto request = new CommentCreateDto();
        request.setContent("새로운 댓글입니다.");

        CommentResponseDto expectedResponse = createCommentResponseDto(1L, "새로운 댓글입니다.", accountId, "홍길동", postId);

        given(commentService.createComment(eq(postId), any(CommentCreateDto.class), eq(accountId)))
                .willReturn(expectedResponse);

        // when & then
        mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                        .param("accountId", accountId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("새로운 댓글입니다."))
                .andExpect(jsonPath("$.authorName").value("홍길동"))
                .andExpect(jsonPath("$.postId").value(postId));

        verify(commentService).createComment(eq(postId), any(CommentCreateDto.class), eq(accountId));
    }

    @Test
    @DisplayName("댓글 생성 시 내용이 비어있으면 400 Bad Request를 반환한다")
    void createComment_EmptyContent_Returns400BadRequest() throws Exception {
        // given
        Long postId = 1L;
        Long accountId = 100L;

        CommentCreateDto request = new CommentCreateDto();
        request.setContent("");  // 빈 내용

        // when & then
        mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                        .param("accountId", accountId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("댓글 목록 조회 시 정상적인 요청이면 200 OK와 함께 페이징된 댓글 목록을 반환한다")
    void getComments_ValidRequest_Returns200OK() throws Exception {
        // given
        Long postId = 1L;

        CommentResponseDto comment1 = createCommentResponseDto(1L, "첫 번째 댓글", 100L, "홍길동", postId);
        CommentResponseDto comment2 = createCommentResponseDto(2L, "두 번째 댓글", 101L, "김철수", postId);

        Page<CommentResponseDto> expectedPage = new PageImpl<>(
                List.of(comment1, comment2),
                PageRequest.of(0, 5),
                2L
        );

        given(commentService.getComments(eq(postId), any()))
                .willReturn(expectedPage);

        // when & then
        mockMvc.perform(get("/api/posts/{postId}/comments", postId)
                        .param("page", "0")
                        .param("size", "5"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].content").value("첫 번째 댓글"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("댓글 목록 조회 시 페이지 번호가 음수이면 400 Bad Request를 반환한다")
    void getComments_NegativePage_Returns400BadRequest() throws Exception {
        // given
        Long postId = 1L;

        // when & then
        mockMvc.perform(get("/api/posts/{postId}/comments", postId)
                        .param("page", "-1")  // 음수 페이지
                        .param("size", "5"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("댓글 목록 조회 시 페이지 크기가 0 이하이면 400 Bad Request를 반환한다")
    void getComments_InvalidSize_Returns400BadRequest() throws Exception {
        // given
        Long postId = 1L;

        // when & then
        mockMvc.perform(get("/api/posts/{postId}/comments", postId)
                        .param("page", "0")
                        .param("size", "0"))  // 0 크기
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("댓글 목록 조회 시 페이지 크기가 100 초과이면 400 Bad Request를 반환한다")
    void getComments_ExceedsMaxSize_Returns400BadRequest() throws Exception {
        // given
        Long postId = 1L;

        // when & then
        mockMvc.perform(get("/api/posts/{postId}/comments", postId)
                        .param("page", "0")
                        .param("size", "101"))  // 최대 크기 초과
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // 테스트 헬퍼 메서드
    private CommentResponseDto createCommentResponseDto(Long id, String content, Long authorId, String authorName, Long postId) {
        CommentResponseDto dto = new CommentResponseDto();
        dto.setId(id);
        dto.setContent(content);
        dto.setAuthorId(authorId);
        dto.setAuthorName(authorName);
        dto.setPostId(postId);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        return dto;
    }
}
