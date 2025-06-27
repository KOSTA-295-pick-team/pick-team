package com.pickteam.controller.board;

import com.pickteam.dto.board.CommentResponseDto;
import com.pickteam.dto.board.CommentUpdateDto;
import com.pickteam.service.board.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentDetailController {

    private final CommentService commentService;

    // 댓글 수정
    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateDto dto,
            @RequestParam Long accountId) {

        CommentResponseDto comment = commentService.updateComment(commentId, dto, accountId);
        return ResponseEntity.ok(comment);
    }

    // 내 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteMyComment(
            @PathVariable Long commentId,
            @RequestParam Long accountId) {

        commentService.deleteMyComment(commentId, accountId);
        return ResponseEntity.noContent().build();
    }

    // 다른 사람의 댓글 삭제 (관리자용)
    @DeleteMapping("/{commentId}/admin")
    public ResponseEntity<Void> deleteUserComment(@PathVariable Long commentId) {
        commentService.deleteUserComment(commentId);
        return ResponseEntity.noContent().build();
    }
}