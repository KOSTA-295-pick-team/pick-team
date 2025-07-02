package com.pickteam.controller;

import com.pickteam.dto.user.UserSummaryResponse;
import com.pickteam.dto.workspace.*;
import com.pickteam.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {
    
    private final WorkspaceService workspaceService;
    
    /**
     * 워크스페이스 생성
     */
    @PostMapping
    public ResponseEntity<WorkspaceResponse> createWorkspace(
            @RequestHeader("User-Id") Long userId,
            @Valid @RequestBody WorkspaceCreateRequest request) {
        WorkspaceResponse response = workspaceService.createWorkspace(userId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 초대 링크로 워크스페이스 참여
     */
    @PostMapping("/join")
    public ResponseEntity<WorkspaceResponse> joinWorkspace(
            @RequestHeader("User-Id") Long userId,
            @Valid @RequestBody WorkspaceJoinRequest request) {
        WorkspaceResponse response = workspaceService.joinWorkspace(userId, request);
        return ResponseEntity.ok(response);
    }

    
    /**
     * 사용자가 속한 워크스페이스 목록 조회
     */
    @GetMapping("/my")
    public ResponseEntity<List<WorkspaceResponse>> getUserWorkspaces(
            @RequestHeader("User-Id") Long userId) {
        List<WorkspaceResponse> workspaces = workspaceService.getUserWorkspaces(userId);
        return ResponseEntity.ok(workspaces);
    }
    
    /**
     * 워크스페이스 상세 조회
     */
    @GetMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceResponse> getWorkspace(
            @PathVariable Long workspaceId,
            @RequestHeader("User-Id") Long userId) {
        WorkspaceResponse workspace = workspaceService.getWorkspace(workspaceId, userId);
        return ResponseEntity.ok(workspace);
    }
    
    /**
     * 워크스페이스 멤버 목록 조회
     */
    @GetMapping("/{workspaceId}/members")
    public ResponseEntity<List<UserSummaryResponse>> getWorkspaceMembers(
            @PathVariable Long workspaceId,
            @RequestHeader("User-Id") Long userId) {
        List<UserSummaryResponse> members = workspaceService.getWorkspaceMembers(workspaceId, userId);
        return ResponseEntity.ok(members);
    }
    
    /**
     * 워크스페이스 설정 업데이트
     */
    @PutMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceResponse> updateWorkspace(
            @PathVariable Long workspaceId,
            @RequestHeader("User-Id") Long userId,
            @Valid @RequestBody WorkspaceUpdateRequest request) {
        WorkspaceResponse response = workspaceService.updateWorkspace(workspaceId, userId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 새 초대 링크 생성
     */
    @PostMapping("/{workspaceId}/invite-code")
    public ResponseEntity<Map<String, String>> generateNewInviteCode(
            @PathVariable Long workspaceId,
            @RequestHeader("User-Id") Long userId) {
        String newInviteCode = workspaceService.generateNewInviteCode(workspaceId, userId);
        
        Map<String, String> response = new HashMap<>();
        response.put("inviteCode", newInviteCode);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 멤버 내보내기
     */
    @DeleteMapping("/{workspaceId}/members/{targetUserId}")
    public ResponseEntity<Void> kickMember(
            @PathVariable Long workspaceId,
            @PathVariable Long targetUserId,
            @RequestHeader("User-Id") Long userId) {
        workspaceService.kickMember(workspaceId, targetUserId, userId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 멤버 차단
     */
    @PostMapping("/{workspaceId}/members/{targetUserId}/ban")
    public ResponseEntity<Void> banMember(
            @PathVariable Long workspaceId,
            @PathVariable Long targetUserId,
            @RequestHeader("User-Id") Long userId) {
        workspaceService.banMember(workspaceId, targetUserId, userId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 워크스페이스 삭제
     */
    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<Void> deleteWorkspace(
            @PathVariable Long workspaceId,
            @RequestHeader("User-Id") Long userId) {
        workspaceService.deleteWorkspace(workspaceId, userId);
        return ResponseEntity.ok().build();
    }
} 