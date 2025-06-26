package com.pickteam.controller.board;

import com.pickteam.dto.board.PostResponseDto;
import com.pickteam.dto.board.PostUpdateDto;
import com.pickteam.service.board.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostDetailController {

    private final PostService postService;

    // 게시글 읽기
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDto> readPost(@PathVariable Long postId) {
        PostResponseDto post = postService.readPost(postId);
        return ResponseEntity.ok(post);
    }

    // 게시글 수정
    @PatchMapping("/{postId}")
    public ResponseEntity<PostResponseDto> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateDto dto,
            @RequestParam Long accountId) {

        PostResponseDto post = postService.updatePost(postId, dto, accountId);
        return ResponseEntity.ok(post);
    }

    // 내 게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deleteMyPost(
            @PathVariable Long postId,
            @RequestParam Long accountId) {

        postService.deleteMyPost(postId, accountId);
        return ResponseEntity.noContent().build();
    }

    // 다른 사람의 게시글 삭제 (관리자용)
    @DeleteMapping("/{postId}/admin")
    public ResponseEntity<Void> deleteUserPost(@PathVariable Long postId) {
        postService.deleteUserPost(postId);
        return ResponseEntity.noContent().build();
    }
}