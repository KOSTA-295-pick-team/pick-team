package mvc.pickteam.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import mvc.pickteam.dto.user.UserSummaryResponse;
import mvc.pickteam.dto.workspace.*;
import mvc.pickteam.service.WorkspaceService;

import jakarta.validation.Valid;
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
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody WorkspaceCreateRequest request) {
        
        WorkspaceResponse response = workspaceService.createWorkspace(userId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 초대 링크로 워크스페이스 참여
     */
    @PostMapping("/join")
    public ResponseEntity<WorkspaceResponse> joinWorkspace(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody WorkspaceJoinRequest request) {
        
        WorkspaceResponse response = workspaceService.joinWorkspace(userId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 내 워크스페이스 목록 조회
     */
    @GetMapping("/my")
    public ResponseEntity<List<WorkspaceResponse>> getMyWorkspaces(
            @RequestHeader("X-User-Id") Long userId) {
        
        List<WorkspaceResponse> workspaces = workspaceService.getUserWorkspaces(userId);
        return ResponseEntity.ok(workspaces);
    }
    
    /**
     * 워크스페이스 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<WorkspaceResponse> getWorkspace(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        
        WorkspaceResponse response = workspaceService.getWorkspace(id, userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 워크스페이스 멤버 목록 조회
     */
    @GetMapping("/{id}/members")
    public ResponseEntity<List<UserSummaryResponse>> getWorkspaceMembers(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        
        List<UserSummaryResponse> members = workspaceService.getWorkspaceMembers(id, userId);
        return ResponseEntity.ok(members);
    }
    
    /**
     * 워크스페이스 설정 업데이트
     */
    @PutMapping("/{id}")
    public ResponseEntity<WorkspaceResponse> updateWorkspace(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody WorkspaceUpdateRequest request) {
        
        WorkspaceResponse response = workspaceService.updateWorkspace(id, userId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 새 초대 링크 생성
     */
    @PostMapping("/{id}/invite-code/regenerate")
    public ResponseEntity<Map<String, String>> regenerateInviteCode(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        
        String newInviteCode = workspaceService.generateNewInviteCode(id, userId);
        return ResponseEntity.ok(Map.of("inviteCode", newInviteCode));
    }
    
    /**
     * 멤버 내보내기
     */
    @DeleteMapping("/{id}/members/{userId}/kick")
    public ResponseEntity<Void> kickMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            @RequestHeader("X-User-Id") Long requestUserId) {
        
        workspaceService.kickMember(id, userId, requestUserId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 멤버 영구 밴
     */
    @DeleteMapping("/{id}/members/{userId}/ban")
    public ResponseEntity<Void> banMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            @RequestHeader("X-User-Id") Long requestUserId) {
        
        workspaceService.banMember(id, userId, requestUserId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 워크스페이스 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkspace(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        
        workspaceService.deleteWorkspace(id, userId);
        return ResponseEntity.ok().build();
    }
} 