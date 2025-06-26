package com.pickteam.controller.board;

import com.pickteam.dto.board.CommentCreateDto;
import com.pickteam.dto.board.CommentResponseDto;
import com.pickteam.service.board.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    // 댓글 생성
    @PostMapping
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateDto dto,
            @RequestParam Long accountId) {

        CommentResponseDto comment = commentService.createComment(postId, dto, accountId);
        return ResponseEntity.ok(comment);
    }


    // 댓글 목록 조회
    @GetMapping
    public ResponseEntity<Page<CommentResponseDto>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        Page<CommentResponseDto> comments = commentService.getComments(postId, pageable);
        return ResponseEntity.ok(comments);
    }
}