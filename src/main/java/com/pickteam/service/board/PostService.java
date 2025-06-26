package com.pickteam.service.board;

import com.pickteam.domain.board.Board;
import com.pickteam.domain.board.Post;
import com.pickteam.domain.user.Account;
import com.pickteam.dto.board.*;
import com.pickteam.repository.board.BoardRepository;
import com.pickteam.repository.board.PostRepository;
import com.pickteam.repository.user.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final AccountRepository accountRepository;
    private final BoardRepository boardRepository;

    public Page<PostResponseDto> getPosts(Long boardId, Pageable pageable) {
        Page<Post> posts = postRepository.findPostsWithCommentsCount(boardId, pageable);
        return posts.map(PostResponseDto::from);
    }

    public PostResponseDto readPost(Long postId) {
        Post post = postRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        return PostResponseDto.from(post);
    }

    @Transactional
    public PostResponseDto createPost(PostCreateDto dto, Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Board board = boardRepository.findById(dto.getBoardId())
                .orElseThrow(() -> new IllegalArgumentException("게시판을 찾을 수 없습니다."));

        Integer nextPostNo = postRepository.findMaxPostNoByBoardId(dto.getBoardId()) + 1;

        Post post = Post.builder()
                .postNo(nextPostNo)
                .title(dto.getTitle())
                .content(dto.getContent())
                .account(account)
                .board(board)
                .build();

        Post savedPost = postRepository.save(post);
        return PostResponseDto.from(savedPost);
    }

    @Transactional
    public PostResponseDto updatePost(Long postId, PostUpdateDto dto, Long accountId) {
        Post post = postRepository.findByIdWithDetails(postId)
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

        postRepository.delete(post);
    }

    @Transactional
    public void deleteUserPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        postRepository.delete(post);
    }

    private void validatePostOwner(Post post, Long accountId) {
        if (!post.getAccount().getId().equals(accountId)) {
            throw new IllegalArgumentException("게시글 작성자만 수정/삭제할 수 있습니다.");
        }
    }
}