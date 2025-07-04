package com.pickteam.service.board;

import com.pickteam.domain.board.Comment;
import com.pickteam.domain.board.Post;
import com.pickteam.domain.user.Account;
import com.pickteam.dto.board.CommentCreateDto;
import com.pickteam.dto.board.CommentResponseDto;
import com.pickteam.dto.board.CommentUpdateDto;
import com.pickteam.repository.board.CommentRepository;
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
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final AccountRepository accountRepository;

    public Page<CommentResponseDto> getComments(Long postId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findByPostIdWithAuthorAndIsDeletedFalse(postId, pageable);
        return comments.map(CommentResponseDto::from);
    }

    @Transactional
    public CommentResponseDto createComment(Long postId, CommentCreateDto dto, Long accountId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Comment comment = Comment.builder()
                .content(dto.getContent())
                .post(post)
                .account(account)
                .build();

        Comment savedComment = commentRepository.save(comment);
        return CommentResponseDto.from(savedComment);
    }

    @Transactional
    public CommentResponseDto updateComment(Long commentId, CommentUpdateDto dto, Long accountId) {
        Comment comment = commentRepository.findByIdWithDetailsAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        validateCommentOwner(comment, accountId);

        comment.setContent(dto.getContent());

        return CommentResponseDto.from(comment);
    }

    @Transactional
    public void deleteMyComment(Long commentId, Long accountId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        validateCommentOwner(comment, accountId);

        // 수동 Soft Delete 처리
        comment.markDeleted();
    }

    @Transactional
    public void deleteUserComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 수동 Soft Delete 처리
        comment.markDeleted();
    }

    private void validateCommentOwner(Comment comment, Long accountId) {
        if (!comment.getAccount().getId().equals(accountId)) {
            throw new IllegalArgumentException("댓글 작성자만 수정/삭제할 수 있습니다.");
        }
    }
}