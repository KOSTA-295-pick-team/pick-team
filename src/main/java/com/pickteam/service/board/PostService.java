package com.pickteam.service.board;

import com.pickteam.domain.board.Board;
import com.pickteam.domain.board.Post;
import com.pickteam.domain.user.Account;
import com.pickteam.dto.board.*;
import com.pickteam.repository.board.BoardRepository;
import com.pickteam.repository.board.PostRepository;
import com.pickteam.repository.user.AccountRepository;
import com.pickteam.service.board.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final AccountRepository accountRepository;
    private final BoardRepository boardRepository;
    private final BoardService boardService;

    public Page<PostResponseDto> getPosts(Long boardId, Pageable pageable) {
        Page<Post> posts = postRepository.findPostsWithCommentsCount(boardId, pageable);
        return posts.map(PostResponseDto::from);
    }

    /**
     * 팀 ID로 게시글 목록 조회 (게시판 자동 생성 포함)
     */
    public Page<PostResponseDto> getPostsByTeamId(Long teamId, Pageable pageable) {
        Board board = boardService.getBoardByTeamId(teamId);
        return getPosts(board.getId(), pageable);
    }

    public PostResponseDto readPost(Long postId) {
        Post post = postRepository.findByIdWithDetailsAndIsDeletedFalse(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 지연 로딩으로 컬렉션 초기화 (null 체크 포함)
        if (post.getAttachments() != null) {
            post.getAttachments().size();
        }
        if (post.getComments() != null) {
            post.getComments().size();
        }

        return PostResponseDto.from(post);
    }

    @Transactional
    public PostResponseDto createPost(PostCreateDto dto, Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. accountId: " + accountId));

        Board board = boardRepository.findByIdAndIsDeletedFalse(dto.getBoardId())
                .orElseThrow(() -> new IllegalArgumentException("게시판을 찾을 수 없습니다. boardId: " + dto.getBoardId()));

        Integer nextPostNo = postRepository.findMaxPostNoByBoardIdAndIsDeletedFalse(dto.getBoardId()) + 1;

        Post post = Post.builder()
                .postNo(nextPostNo)
                .title(dto.getTitle())
                .content(dto.getContent())
                .account(account)
                .board(board)
                .attachments(new ArrayList<>())  // 명시적으로 빈 리스트 초기화
                .comments(new ArrayList<>())     // 명시적으로 빈 리스트 초기화
                .build();

        Post savedPost = postRepository.save(post);

        // 저장된 객체를 바로 DTO로 변환 (재조회 불필요)
        return PostResponseDto.from(savedPost);
    }

    /**
     * 팀 ID로 게시글 생성 (게시판 자동 생성 포함)
     */
    @Transactional
    public PostResponseDto createPostByTeamId(Long teamId, PostCreateDto dto, Long accountId) {
        // 팀의 게시판을 찾거나 자동 생성
        Board board = boardService.getBoardByTeamId(teamId);
        
        // DTO에 게시판 ID 설정
        dto.setBoardId(board.getId());
        
        return createPost(dto, accountId);
    }

    @Transactional
    public PostResponseDto updatePost(Long postId, PostUpdateDto dto, Long accountId) {
        Post post = postRepository.findByIdWithDetailsAndIsDeletedFalse(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        validatePostOwner(post, accountId);

        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());

        return PostResponseDto.from(post);
    }

    @Transactional
    public void deleteMyPost(Long postId, Long accountId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        validatePostOwner(post, accountId);

        // Soft Delete 처리
        post.markDeleted();
    }

    @Transactional
    public void deleteUserPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 수동 Soft Delete 처리
        post.markDeleted();
    }

    private void validatePostOwner(Post post, Long accountId) {
        if (!post.getAccount().getId().equals(accountId)) {
            throw new IllegalArgumentException("게시글 작성자만 수정/삭제할 수 있습니다.");
        }
    }
}