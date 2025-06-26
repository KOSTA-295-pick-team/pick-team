package com.pickteam.controller.board;

import com.pickteam.dto.board.CommentCreateDto;
import com.pickteam.dto.board.CommentResponseDto;
import com.pickteam.service.board.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 쓰기
    @PostMapping
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateDto dto,
            @RequestParam Long accountId) {

        CommentResponseDto comment = commentService.createComment(postId, dto, accountId);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    // 댓글 조회
    @GetMapping
    public ResponseEntity<Page<CommentResponseDto>> getComments(
            @PathVariable Long postId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<CommentResponseDto> comments = commentService.getComments(postId, pageable);
        return ResponseEntity.ok(comments);
    }
}