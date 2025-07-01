package com.pickteam.controller.team;

import com.pickteam.dto.ApiResponse;
import com.pickteam.dto.team.*;
import com.pickteam.security.UserPrincipal;
import com.pickteam.service.team.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {
    
    private final TeamService teamService;
    
    /**
     * 현재 인증된 사용자의 ID를 가져오는 헬퍼 메서드
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getId();
    }
    
    /**
     * 팀 생성 (워크스페이스의 모든 멤버 가능)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TeamResponse>> createTeam(
            @Valid @RequestBody TeamCreateRequest request) {
        Long userId = getCurrentUserId();
        TeamResponse response = teamService.createTeam(userId, request);
        return ResponseEntity.ok(ApiResponse.success("팀 생성 성공", response));
    }
    
    /**
     * 워크스페이스의 팀 목록 조회
     */
    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<ApiResponse<List<TeamResponse>>> getTeamsByWorkspace(
            @PathVariable Long workspaceId) {
        Long userId = getCurrentUserId();
        List<TeamResponse> teams = teamService.getTeamsByWorkspace(workspaceId, userId);
        return ResponseEntity.ok(ApiResponse.success("팀 목록 조회 성공", teams));
    }
    
    /**
     * 팀 상세 조회
     */
    @GetMapping("/{teamId}")
    public ResponseEntity<ApiResponse<TeamResponse>> getTeam(
            @PathVariable Long teamId) {
        Long userId = getCurrentUserId();
        TeamResponse team = teamService.getTeam(teamId, userId);
        return ResponseEntity.ok(ApiResponse.success("팀 조회 성공", team));
    }
    
    /**
     * 팀 수정 (팀장만 가능)
     */
    @PutMapping("/{teamId}")
    public ResponseEntity<ApiResponse<TeamResponse>> updateTeam(
            @PathVariable Long teamId,
            @Valid @RequestBody TeamUpdateRequest request) {
        Long userId = getCurrentUserId();
        TeamResponse response = teamService.updateTeam(teamId, userId, request);
        return ResponseEntity.ok(ApiResponse.success("팀 수정 성공", response));
    }
    
    /**
     * 팀 삭제 (팀장 + 워크스페이스 관리자 가능)
     */
    @DeleteMapping("/{teamId}")
    public ResponseEntity<ApiResponse<Void>> deleteTeam(
            @PathVariable Long teamId) {
        Long userId = getCurrentUserId();
        teamService.deleteTeam(teamId, userId);
        return ResponseEntity.ok(ApiResponse.success("팀 삭제 성공", null));
    }
    
    /**
     * 팀 참여
     */
    @PostMapping("/{teamId}/join")
    public ResponseEntity<ApiResponse<Void>> joinTeam(
            @PathVariable Long teamId) {
        Long userId = getCurrentUserId();
        teamService.joinTeam(teamId, userId);
        return ResponseEntity.ok(ApiResponse.success("팀 참여 성공", null));
    }
    
    /**
     * 팀 탈퇴
     */
    @PostMapping("/{teamId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveTeam(
            @PathVariable Long teamId) {
        Long userId = getCurrentUserId();
        teamService.leaveTeam(teamId, userId);
        return ResponseEntity.ok(ApiResponse.success("팀 탈퇴 성공", null));
    }
    
    /**
     * 팀 멤버 목록 조회
     */
    @GetMapping("/{teamId}/members")
    public ResponseEntity<ApiResponse<List<TeamMemberResponse>>> getTeamMembers(
            @PathVariable Long teamId) {
        Long userId = getCurrentUserId();
        List<TeamMemberResponse> members = teamService.getTeamMembers(teamId, userId);
        return ResponseEntity.ok(ApiResponse.success("팀 멤버 목록 조회 성공", members));
    }
} 