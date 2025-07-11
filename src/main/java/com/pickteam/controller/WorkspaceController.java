package com.pickteam.controller;

import com.pickteam.dto.ApiResponse;
import com.pickteam.dto.user.UserSummaryResponse;
import com.pickteam.dto.workspace.*;
import com.pickteam.security.UserPrincipal;
import com.pickteam.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
     * 현재 인증된 사용자의 ID를 가져오는 헬퍼 메서드
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getId();
    }
    
    /**
     * 워크스페이스 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<WorkspaceResponse>> createWorkspace(
            @Valid @RequestBody WorkspaceCreateRequest request) {
        Long userId = getCurrentUserId();
        WorkspaceResponse response = workspaceService.createWorkspace(userId, request);
        return ResponseEntity.ok(ApiResponse.success("워크스페이스 생성 성공", response));
    }
    
    /**
     * 초대 링크로 워크스페이스 참여
     */
    @PostMapping("/join")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> joinWorkspace(
            @Valid @RequestBody WorkspaceJoinRequest request) {
        Long userId = getCurrentUserId();
        WorkspaceResponse response = workspaceService.joinWorkspace(userId, request);
        return ResponseEntity.ok(ApiResponse.success("워크스페이스 참여 성공", response));
    }
    
    /**
     * 워크스페이스 ID로 직접 참여
     */
    @PostMapping("/{workspaceId}/join")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> joinWorkspaceById(
            @PathVariable Long workspaceId,
            @RequestBody(required = false) Map<String, String> requestBody) {
        Long userId = getCurrentUserId();
        String password = requestBody != null ? requestBody.get("password") : null;
        WorkspaceResponse response = workspaceService.joinWorkspaceById(userId, workspaceId, password);
        return ResponseEntity.ok(ApiResponse.success("워크스페이스 참여 성공", response));
    }

    
    /**
     * 사용자가 속한 워크스페이스 목록 조회
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<WorkspaceResponse>>> getUserWorkspaces() {
        Long userId = getCurrentUserId();
        List<WorkspaceResponse> workspaces = workspaceService.getUserWorkspaces(userId);
        return ResponseEntity.ok(ApiResponse.success("워크스페이스 목록 조회 성공", workspaces));
    }
    
    /**
     * 워크스페이스 상세 조회
     */
    @GetMapping("/{workspaceId}")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> getWorkspace(
            @PathVariable Long workspaceId) {
        Long userId = getCurrentUserId();
        WorkspaceResponse workspace = workspaceService.getWorkspace(workspaceId, userId);
        return ResponseEntity.ok(ApiResponse.success("워크스페이스 조회 성공", workspace));
    }
    
    /**
     * 워크스페이스 멤버 목록 조회
     */
    @GetMapping("/{workspaceId}/members")
    public ResponseEntity<ApiResponse<List<UserSummaryResponse>>> getWorkspaceMembers(
            @PathVariable Long workspaceId) {
        Long userId = getCurrentUserId();
        List<UserSummaryResponse> members = workspaceService.getWorkspaceMembers(workspaceId, userId);
        return ResponseEntity.ok(ApiResponse.success("워크스페이스 멤버 목록 조회 성공", members));
    }
    
    /**
     * 워크스페이스 설정 업데이트
     */
    @PutMapping("/{workspaceId}")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> updateWorkspace(
            @PathVariable Long workspaceId,
            @Valid @RequestBody WorkspaceUpdateRequest request) {
        Long userId = getCurrentUserId();
        WorkspaceResponse response = workspaceService.updateWorkspace(workspaceId, userId, request);
        return ResponseEntity.ok(ApiResponse.success("워크스페이스 업데이트 성공", response));
    }
    
    /**
     * 새 초대 링크 생성
     */
    @PostMapping("/{workspaceId}/invite-code")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateNewInviteCode(
            @PathVariable Long workspaceId) {
        Long userId = getCurrentUserId();
        String newInviteCode = workspaceService.generateNewInviteCode(workspaceId, userId);
        
        Map<String, String> response = new HashMap<>();
        response.put("inviteCode", newInviteCode);
        
        return ResponseEntity.ok(ApiResponse.success("초대 링크 생성 성공", response));
    }
    
    /**
     * 멤버 내보내기
     */
    @DeleteMapping("/{workspaceId}/members/{targetUserId}")
    public ResponseEntity<ApiResponse<Void>> kickMember(
            @PathVariable Long workspaceId,
            @PathVariable Long targetUserId) {
        Long userId = getCurrentUserId();
        workspaceService.kickMember(workspaceId, targetUserId, userId);
        return ResponseEntity.ok(ApiResponse.success("멤버 내보내기 성공", null));
    }
    
    /**
     * 멤버 차단
     */
    @PostMapping("/{workspaceId}/members/{targetUserId}/ban")
    public ResponseEntity<ApiResponse<Void>> banMember(
            @PathVariable Long workspaceId,
            @PathVariable Long targetUserId) {
        Long userId = getCurrentUserId();
        workspaceService.banMember(workspaceId, targetUserId, userId);
        return ResponseEntity.ok(ApiResponse.success("멤버 차단 성공", null));
    }
    
    /**
     * 멤버 차단 해제
     */
    @DeleteMapping("/{workspaceId}/members/{targetUserId}/ban")
    public ResponseEntity<ApiResponse<Void>> unbanMember(
            @PathVariable Long workspaceId,
            @PathVariable Long targetUserId) {
        Long userId = getCurrentUserId();
        workspaceService.unbanMember(workspaceId, targetUserId, userId);
        return ResponseEntity.ok(ApiResponse.success("멤버 차단 해제 성공", null));
    }
    
    /**
     * 워크스페이스 블랙리스트 목록 조회
     */
    @GetMapping("/{workspaceId}/blacklist")
    public ResponseEntity<ApiResponse<List<UserSummaryResponse>>> getBlacklistedMembers(
            @PathVariable Long workspaceId) {
        Long userId = getCurrentUserId();
        List<UserSummaryResponse> blacklistedMembers = workspaceService.getBlacklistedMembers(workspaceId, userId);
        return ResponseEntity.ok(ApiResponse.success("블랙리스트 조회 성공", blacklistedMembers));
    }
    
    /**
     * 워크스페이스 삭제
     */
    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<ApiResponse<Void>> deleteWorkspace(
            @PathVariable Long workspaceId) {
        Long userId = getCurrentUserId();
        workspaceService.deleteWorkspace(workspaceId, userId);
        return ResponseEntity.ok(ApiResponse.success("워크스페이스 삭제 성공", null));
    }
} 