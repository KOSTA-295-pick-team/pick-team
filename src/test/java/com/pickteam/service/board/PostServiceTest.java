package com.pickteam.service.board;

import com.pickteam.domain.board.Board;
import com.pickteam.domain.board.Post;
import com.pickteam.domain.user.Account;
import com.pickteam.dto.board.PostCreateDto;
import com.pickteam.dto.board.PostResponseDto;
import com.pickteam.dto.board.PostUpdateDto;
import com.pickteam.repository.board.BoardRepository;
import com.pickteam.repository.board.PostRepository;
import com.pickteam.repository.user.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * 게시글 서비스 단위 테스트
 * Business Layer 로직 검증에 집중
 */
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private BoardRepository boardRepository;

    @Test
    @DisplayName("게시글 생성 시 유효한 요청이면 게시글을 성공적으로 생성한다")
    void createPost_ValidRequest_ReturnsPostResponse() {
        // given
        Long accountId = 100L;
        Long boardId = 10L;
        
        PostCreateDto request = new PostCreateDto();
        request.setTitle("새로운 게시글");
        request.setContent("게시글 내용입니다.");
        request.setBoardId(boardId);

        Account mockAccount = Account.builder()
                .id(accountId)
                .email("test@example.com")
                .name("홍길동")
                .build();

        Board mockBoard = Board.builder()
                .id(boardId)
                .build();

        Post mockPost = Post.builder()
                .id(1L)
                .postNo(1)
                .title(request.getTitle())
                .content(request.getContent())
                .account(mockAccount)
                .board(mockBoard)
                .attachments(Collections.emptyList())
                .comments(Collections.emptyList())
                .build();

        // Mock 설정
        given(accountRepository.findById(accountId)).willReturn(Optional.of(mockAccount));
        given(boardRepository.findById(boardId)).willReturn(Optional.of(mockBoard));
        given(postRepository.save(any(Post.class))).willReturn(mockPost);

        // when
        PostResponseDto result = postService.createPost(request, accountId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("새로운 게시글");
        assertThat(result.getContent()).isEqualTo("게시글 내용입니다.");
        assertThat(result.getAuthorId()).isEqualTo(accountId);
        assertThat(result.getAuthorName()).isEqualTo("홍길동");
        assertThat(result.getBoardId()).isEqualTo(boardId);

        verify(accountRepository).findById(accountId);
        verify(boardRepository).findById(boardId);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 생성 시 존재하지 않는 계정이면 예외를 발생시킨다")
    void createPost_AccountNotFound_ThrowsIllegalArgumentException() {
        // given
        Long invalidAccountId = 999L;
        PostCreateDto request = new PostCreateDto();
        request.setTitle("게시글 제목");
        request.setContent("게시글 내용");
        request.setBoardId(10L);

        given(accountRepository.findById(invalidAccountId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.createPost(request, invalidAccountId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");

        verify(accountRepository).findById(invalidAccountId);
    }

    @Test
    @DisplayName("게시글 생성 시 존재하지 않는 게시판이면 예외를 발생시킨다")
    void createPost_BoardNotFound_ThrowsIllegalArgumentException() {
        // given
        Long accountId = 100L;
        Long invalidBoardId = 999L;
        
        PostCreateDto request = new PostCreateDto();
        request.setTitle("게시글 제목");
        request.setContent("게시글 내용");
        request.setBoardId(invalidBoardId);

        Account mockAccount = Account.builder()
                .id(accountId)
                .email("test@example.com")
                .name("홍길동")
                .build();

        given(accountRepository.findById(accountId)).willReturn(Optional.of(mockAccount));
        given(boardRepository.findById(invalidBoardId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.createPost(request, accountId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("게시판을 찾을 수 없습니다");

        verify(accountRepository).findById(accountId);
        verify(boardRepository).findById(invalidBoardId);
    }

    @Test
    @DisplayName("게시글 목록 조회 시 유효한 게시판 ID이면 페이징된 게시글 목록을 반환한다")
    void getPosts_ValidBoardId_ReturnsPagedPosts() {
        // given
        Long boardId = 10L;
        Pageable pageable = PageRequest.of(0, 5);
        
        Account mockAccount = Account.builder()
                .id(100L)
                .name("홍길동")
                .build();

        Board mockBoard = Board.builder()
                .id(boardId)
                .build();

        Post post1 = Post.builder()
                .id(1L)
                .postNo(1)
                .title("첫 번째 게시글")
                .content("첫 번째 내용")
                .account(mockAccount)
                .board(mockBoard)
                .attachments(Collections.emptyList())
                .comments(Collections.emptyList())
                .build();

        Post post2 = Post.builder()
                .id(2L)
                .postNo(2)
                .title("두 번째 게시글")
                .content("두 번째 내용")
                .account(mockAccount)
                .board(mockBoard)
                .attachments(Collections.emptyList())
                .comments(Collections.emptyList())
                .build();

        Page<Post> mockPage = new PageImpl<>(List.of(post1, post2), pageable, 2L);

        given(postRepository.findPostsWithCommentsCount(boardId, pageable))
                .willReturn(mockPage);

        // when
        Page<PostResponseDto> result = postService.getPosts(boardId, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("첫 번째 게시글");
        assertThat(result.getContent().get(1).getTitle()).isEqualTo("두 번째 게시글");
        assertThat(result.getTotalElements()).isEqualTo(2L);

        verify(postRepository).findPostsWithCommentsCount(boardId, pageable);
    }

    @Test
    @DisplayName("게시글 상세 조회 시 유효한 ID이면 게시글 상세 정보를 반환한다")
    void readPost_ValidId_ReturnsPostDetail() {
        // given
        Long postId = 1L;
        
        Account mockAccount = Account.builder()
                .id(100L)
                .name("홍길동")
                .build();

        Board mockBoard = Board.builder()
                .id(10L)
                .build();

        Post mockPost = Post.builder()
                .id(postId)
                .postNo(1)
                .title("게시글 제목")
                .content("게시글 내용")
                .account(mockAccount)
                .board(mockBoard)
                .attachments(Collections.emptyList())
                .comments(Collections.emptyList())
                .build();

        given(postRepository.findByIdWithDetailsAndIsDeletedFalse(postId))
                .willReturn(Optional.of(mockPost));

        // when
        PostResponseDto result = postService.readPost(postId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(postId);
        assertThat(result.getTitle()).isEqualTo("게시글 제목");
        assertThat(result.getContent()).isEqualTo("게시글 내용");
        assertThat(result.getAuthorName()).isEqualTo("홍길동");

        verify(postRepository).findByIdWithDetailsAndIsDeletedFalse(postId);
    }

    @Test
    @DisplayName("게시글 상세 조회 시 존재하지 않는 ID이면 예외를 발생시킨다")
    void readPost_InvalidId_ThrowsIllegalArgumentException() {
        // given
        Long invalidPostId = 999L;

        given(postRepository.findByIdWithDetailsAndIsDeletedFalse(invalidPostId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.readPost(invalidPostId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("게시글을 찾을 수 없습니다");

        verify(postRepository).findByIdWithDetailsAndIsDeletedFalse(invalidPostId);
    }
}
