package com.pickteam.controller.board;

import com.pickteam.dto.board.PostAttachResponseDto;
import com.pickteam.service.board.PostAttachService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/attachments")
@RequiredArgsConstructor
public class PostAttachController {

    private final PostAttachService postAttachService;

    // 게시글 첨부파일 목록 조회
    @GetMapping
    public ResponseEntity<List<PostAttachResponseDto>> getPostAttachments(@PathVariable Long postId) {
        List<PostAttachResponseDto> attachments = postAttachService.getPostAttachments(postId);
        return ResponseEntity.ok(attachments);
    }

    // 게시글 첨부파일 업로드
    @PostMapping
    public ResponseEntity<PostAttachResponseDto> uploadPostAttachment(
            @PathVariable Long postId,
            @RequestParam("file") MultipartFile file,
            @RequestParam Long accountId) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 선택되지 않았습니다.");
        }

        PostAttachResponseDto attachment = postAttachService.uploadPostAttachment(postId, file, accountId);
        return ResponseEntity.ok(attachment);
    }

    // 내 게시글 첨부파일 삭제
    @DeleteMapping("/{attachId}")
    public ResponseEntity<Void> deletePostAttachment(
            @PathVariable Long postId,
            @PathVariable Long attachId,
            @RequestParam Long accountId) {

        postAttachService.deletePostAttachment(attachId, accountId);
        return ResponseEntity.noContent().build();
    }

    // 다른 사람의 게시글 첨부파일 삭제 (관리자용)
    @DeleteMapping("/{attachId}/admin")
    public ResponseEntity<Void> deleteUserPostAttachment(
            @PathVariable Long postId,
            @PathVariable Long attachId) {

        postAttachService.deleteUserPostAttachment(attachId);
        return ResponseEntity.noContent().build();
    }
}