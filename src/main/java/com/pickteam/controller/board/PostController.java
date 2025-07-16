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

    // 게시글 목록 조회 (팀 ID로 자동 게시판 찾기)
    @GetMapping
    public ResponseEntity<Page<PostResponseDto>> getPosts(
            @PathVariable Long teamId,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PostResponseDto> posts = postService.getPostsByTeamId(teamId, pageable);
        return ResponseEntity.ok(posts);
    }

    // 기존 방식 지원 (boardId 직접 지정)
    @GetMapping("/board/{boardId}")
    public ResponseEntity<Page<PostResponseDto>> getPostsByBoardId(
            @PathVariable Long teamId,
            @PathVariable Long boardId,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PostResponseDto> posts = postService.getPosts(boardId, pageable);
        return ResponseEntity.ok(posts);
    }

    // 게시글 생성 (팀 ID로 자동 게시판 찾기)
    @PostMapping
    public ResponseEntity<PostResponseDto> createPost(
            @PathVariable Long teamId,
            @Valid @RequestBody PostCreateDto dto,
            @RequestParam Long accountId) {

        PostResponseDto post = postService.createPostByTeamId(teamId, dto, accountId);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    // 기존 방식 지원 (boardId 직접 지정)
    @PostMapping("/board/{boardId}")
    public ResponseEntity<PostResponseDto> createPostWithBoardId(
            @PathVariable Long teamId,
            @PathVariable Long boardId,
            @Valid @RequestBody PostCreateDto dto,
            @RequestParam Long accountId) {

        dto.setBoardId(boardId); // DTO에 boardId 설정
        PostResponseDto post = postService.createPost(dto, accountId);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }
}