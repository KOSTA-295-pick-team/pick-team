package com.pickteam.service.team;

import com.pickteam.domain.team.Team;
import com.pickteam.domain.team.TeamMember;
import com.pickteam.domain.user.Account;
import com.pickteam.domain.workspace.Workspace;
import com.pickteam.domain.workspace.WorkspaceMember;
import com.pickteam.dto.kanban.KanbanCreateRequest;
import com.pickteam.dto.team.*;
import com.pickteam.dto.user.UserSummaryResponse;
import com.pickteam.repository.team.TeamMemberRepository;
import com.pickteam.repository.team.TeamRepository;
import com.pickteam.repository.user.AccountRepository;
import com.pickteam.repository.workspace.WorkspaceMemberRepository;
import com.pickteam.repository.workspace.WorkspaceRepository;
import com.pickteam.service.board.BoardService;
import com.pickteam.service.kanban.KanbanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TeamService {
    
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final AccountRepository accountRepository;
    private final KanbanService kanbanService;
    private final BoardService boardService;
    
    /**
     * 팀 생성 (워크스페이스의 모든 멤버가 가능)
     * 생성자는 자동으로 팀장이 됨
     */
    @Transactional
    public TeamResponse createTeam(Long accountId, TeamCreateRequest request) {
        // 사용자 확인
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 워크스페이스 확인
        Workspace workspace = workspaceRepository.findByIdAndIsDeletedFalse(request.getWorkspaceId())
                .orElseThrow(() -> new RuntimeException("워크스페이스를 찾을 수 없습니다."));
        
        // 활성 워크스페이스 멤버인지 확인 (일반 멤버도 팀 생성 가능)
        if (!workspaceMemberRepository.existsActiveByWorkspaceIdAndAccountId(request.getWorkspaceId(), accountId)) {
            throw new RuntimeException("워크스페이스 멤버만 팀을 생성할 수 있습니다.");
        }
        
        // 팀 생성
        Team team = Team.builder()
                .name(request.getName())
                .workspace(workspace)
                .build();
        
        team = teamRepository.save(team);
        
        // 생성자를 팀장으로 추가
        TeamMember teamLeader = TeamMember.builder()
                .team(team)
                .account(account)
                .teamRole(TeamMember.TeamRole.LEADER)
                .teamStatus(TeamMember.TeamStatus.ACTIVE)
                .build();
        
        teamMemberRepository.save(teamLeader);
        
        // 팀 생성 시 자동으로 게시판 생성
        try {
            boardService.createDefaultBoardForTeam(team.getId());
        } catch (Exception e) {
            // 게시판 생성 실패 시 로그만 출력하고 팀 생성은 계속 진행
            log.error("게시판 자동 생성 실패 - 팀 ID: {}", team.getId(), e);
        }
        
        // 팀 생성 시 자동으로 칸반 보드 생성
        try {
            KanbanCreateRequest kanbanRequest = new KanbanCreateRequest();
            kanbanRequest.setTeamId(team.getId());
            kanbanRequest.setWorkspaceId(workspace.getId());
            kanbanService.createKanban(kanbanRequest);
        } catch (Exception e) {
            // 칸반 보드 생성 실패 시 로그만 출력하고 팀 생성은 계속 진행
            log.error("칸반 보드 자동 생성 실패 - 팀 ID: {}", team.getId(), e);
        }
        
        return convertToResponse(team);
    }
    
    /**
     * 워크스페이스의 팀 목록 조회
     */
    public List<TeamResponse> getTeamsByWorkspace(Long workspaceId, Long accountId) {
        // 활성 워크스페이스 멤버인지 확인
        if (!workspaceMemberRepository.existsActiveByWorkspaceIdAndAccountId(workspaceId, accountId)) {
            throw new RuntimeException("워크스페이스 접근 권한이 없습니다.");
        }
        
        List<Team> teams = teamRepository.findByWorkspaceId(workspaceId);
        return teams.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 팀 상세 조회
     */
    public TeamResponse getTeam(Long teamId, Long accountId) {
        Team team = teamRepository.findByIdAndIsDeletedFalse(teamId)
                .orElseThrow(() -> new RuntimeException("팀을 찾을 수 없습니다."));
        
        // 활성 워크스페이스 멤버인지 확인
        if (!workspaceMemberRepository.existsActiveByWorkspaceIdAndAccountId(team.getWorkspace().getId(), accountId)) {
            throw new RuntimeException("팀 접근 권한이 없습니다.");
        }
        
        return convertToResponse(team);
    }
    
    /**
     * 팀 수정 (팀장만 가능)
     */
    @Transactional
    public TeamResponse updateTeam(Long teamId, Long accountId, TeamUpdateRequest request) {
        Team team = teamRepository.findByIdAndIsDeletedFalse(teamId)
                .orElseThrow(() -> new RuntimeException("팀을 찾을 수 없습니다."));
        
        // 팀장인지 확인
        if (!teamMemberRepository.isTeamLeader(teamId, accountId)) {
            throw new RuntimeException("팀 수정 권한이 없습니다. 팀장만 수정할 수 있습니다.");
        }
        
        if (request.getName() != null) {
            team.setName(request.getName());
        }
        
        team = teamRepository.save(team);
        return convertToResponse(team);
    }
    
    /**
     * 팀 삭제 (팀장 + 워크스페이스 관리자 가능)
     */
    @Transactional
    public void deleteTeam(Long teamId, Long accountId) {
        Team team = teamRepository.findByIdAndIsDeletedFalse(teamId)
                .orElseThrow(() -> new RuntimeException("팀을 찾을 수 없습니다."));
        
        // 권한 확인: 팀장이거나 워크스페이스 관리자여야 함
        boolean isTeamLeader = teamMemberRepository.isTeamLeader(teamId, accountId);
        
        WorkspaceMember workspaceMember = workspaceMemberRepository.findByWorkspaceIdAndAccountId(team.getWorkspace().getId(), accountId)
                .orElse(null);
        boolean isWorkspaceManager = workspaceMember != null && 
                workspaceMember.getStatus() == WorkspaceMember.MemberStatus.ACTIVE &&
                (workspaceMember.getRole() == WorkspaceMember.MemberRole.OWNER || 
                 workspaceMember.getRole() == WorkspaceMember.MemberRole.ADMIN);
        
        if (!isTeamLeader && !isWorkspaceManager) {
            throw new RuntimeException("팀 삭제 권한이 없습니다. 팀장 또는 워크스페이스 관리자만 삭제할 수 있습니다.");
        }
        
        team.markDeleted();
        teamRepository.save(team);
    }
    
    /**
     * 팀 참여
     */
    @Transactional
    public void joinTeam(Long teamId, Long accountId) {
        // 사용자 확인
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        Team team = teamRepository.findByIdAndIsDeletedFalse(teamId)
                .orElseThrow(() -> new RuntimeException("팀을 찾을 수 없습니다."));
        
        // 활성 워크스페이스 멤버인지 확인
        if (!workspaceMemberRepository.existsActiveByWorkspaceIdAndAccountId(team.getWorkspace().getId(), accountId)) {
            throw new RuntimeException("워크스페이스 멤버만 팀에 참여할 수 있습니다.");
        }
        
        // 이미 활성 팀 멤버인지 확인
        if (teamMemberRepository.existsByTeamIdAndAccountId(teamId, accountId)) {
            throw new RuntimeException("이미 팀의 멤버입니다.");
        }
        
        // 기존 팀 멤버 레코드가 있는지 확인 (탈퇴한 멤버 포함)
        Optional<TeamMember> existingMember = teamMemberRepository.findByTeamIdAndAccountId(teamId, accountId);
        
        if (existingMember.isPresent()) {
            // 기존 레코드가 있다면 재활성화
            TeamMember teamMember = existingMember.get();
            teamMember.setTeamStatus(TeamMember.TeamStatus.ACTIVE);
            teamMember.setTeamRole(TeamMember.TeamRole.MEMBER); // 역할 초기화
            teamMemberRepository.save(teamMember);
        } else {
            // 기존 레코드가 없다면 새로 생성
            TeamMember teamMember = TeamMember.builder()
                    .team(team)
                    .account(account)
                    .teamRole(TeamMember.TeamRole.MEMBER)
                    .teamStatus(TeamMember.TeamStatus.ACTIVE)
                    .build();
            
            teamMemberRepository.save(teamMember);
        }
    }
    
    /**
     * 팀 탈퇴
     */
    @Transactional
    public void leaveTeam(Long teamId, Long accountId) {
        TeamMember teamMember = teamMemberRepository.findByTeamIdAndAccountId(teamId, accountId)
                .orElseThrow(() -> new RuntimeException("팀 멤버를 찾을 수 없습니다."));
        
        // 팀장은 탈퇴할 수 없음 (팀을 삭제하거나 팀장을 위임해야 함)
        if (teamMember.getTeamRole() == TeamMember.TeamRole.LEADER) {
            throw new RuntimeException("팀장은 팀을 탈퇴할 수 없습니다. 팀을 삭제하거나 팀장을 위임해주세요.");
        }
        
        teamMember.setTeamStatus(TeamMember.TeamStatus.LEFT);
        teamMemberRepository.save(teamMember);
    }
    
    /**
     * 팀 멤버 목록 조회
     */
    public List<TeamMemberResponse> getTeamMembers(Long teamId, Long accountId) {
        Team team = teamRepository.findByIdAndIsDeletedFalse(teamId)
                .orElseThrow(() -> new RuntimeException("팀을 찾을 수 없습니다."));
        
        // 활성 워크스페이스 멤버인지 확인
        if (!workspaceMemberRepository.existsActiveByWorkspaceIdAndAccountId(team.getWorkspace().getId(), accountId)) {
            throw new RuntimeException("팀 멤버 조회 권한이 없습니다.");
        }
        
        List<TeamMember> members = teamMemberRepository.findActiveMembers(teamId);
        return members.stream()
                .map(this::convertToTeamMemberResponse)
                .collect(Collectors.toList());
    }
    
    private TeamResponse convertToResponse(Team team) {
        List<TeamMember> members = teamMemberRepository.findActiveMembers(team.getId());
        TeamMember leader = teamMemberRepository.findTeamLeader(team.getId()).orElse(null);
        
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .workspaceId(team.getWorkspace().getId())
                .workspaceName(team.getWorkspace().getName())
                .leader(leader != null ? convertToUserSummary(leader.getAccount()) : null)
                .memberCount(members.size())
                .members(members.stream().map(this::convertToTeamMemberResponse).collect(Collectors.toList()))
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .build();
    }
    
    private TeamMemberResponse convertToTeamMemberResponse(TeamMember member) {
        Account account = member.getAccount();
        return TeamMemberResponse.builder()
                .id(member.getId())
                .accountId(account.getId())
                .name(account.getName())
                .email(account.getEmail())
//                .profileImage(account.getProfileImage())
                .teamRole(member.getTeamRole())
                .teamStatus(member.getTeamStatus())
                .joinedAt(member.getCreatedAt())
                // 사용자 상세 정보 추가
                .age(account.getAge())
                .mbti(account.getMbti())
                .disposition(account.getDisposition())
                .introduction(account.getIntroduction())
                .portfolio(account.getPortfolio())
                .preferWorkstyle(account.getPreferWorkstyle())
                .dislikeWorkstyle(account.getDislikeWorkstyle())
                .build();
    }
    
    private UserSummaryResponse convertToUserSummary(Account account) {
        return UserSummaryResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .email(account.getEmail())
                .age(account.getAge())
                .mbti(account.getMbti())
                .disposition(account.getDisposition())
                .introduction(account.getIntroduction())
                .portfolio(account.getPortfolio())
                .preferWorkstyle(account.getPreferWorkstyle())
                .dislikeWorkstyle(account.getDislikeWorkstyle())
                .profileImageUrl(account.getProfileImageUrl())
                .role(account.getRole().toString())
                .build();
    }
}