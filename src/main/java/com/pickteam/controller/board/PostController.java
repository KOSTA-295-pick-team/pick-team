package com.pickteam.controller.board;

import com.pickteam.dto.board.*;
import com.pickteam.service.board.PostService;
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
@RequestMapping("/api/teams/{teamId}/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 게시글 목록 조회
    @GetMapping
    public ResponseEntity<Page<PostResponseDto>> getPosts(
            @PathVariable Long teamId,
            @RequestParam Long boardId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PostResponseDto> posts = postService.getPosts(boardId, pageable);
        return ResponseEntity.ok(posts);
    }

    // 게시글 쓰기
    @PostMapping
    public ResponseEntity<PostResponseDto> createPost(
            @PathVariable Long teamId,
            @Valid @RequestBody PostCreateDto dto,
            @RequestParam Long accountId) {

        PostResponseDto post = postService.createPost(dto, accountId);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }
}