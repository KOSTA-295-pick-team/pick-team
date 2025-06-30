package com.pickteam.service.team;

import com.pickteam.domain.team.Team;
import com.pickteam.domain.team.TeamMember;
import com.pickteam.domain.workspace.Workspace;
import com.pickteam.domain.workspace.WorkspaceMember;
import com.pickteam.dto.team.TeamResponse;
import com.pickteam.dto.workspace.WorkspaceResponse;
import com.pickteam.repository.team.TeamRepository;
import com.pickteam.service.user.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;
    private final TeamMemberService teamMemberService;
    private final AuthService authService; // 추가

    // 팀 id로 팀 가져오기
    @Override
    public TeamResponse getTeamById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));

        return convertToResponse(team);
    }

    @Override
    public Team getTeamByIdForUpdate(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));
        return team;
    }

    // 팀 이름으로 팀 검색하기
    @Override
    public List<TeamResponse> searchByName(String name) {
        List<Team> teamList = teamRepository.findByNameContaining(name);
        //DTO로 가공
        List<TeamResponse> responseList = null;
        if (teamList != null) {
            responseList = teamList.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        }
        return responseList;
    }

    // 사용자 id가 속한 팀 목록 가져오기
    @Override
    public List<TeamResponse> getTeamsByAccount(Long accountId) {
        List<Team> teamList = teamRepository.findByAccountId(accountId);
        List <TeamResponse> responseList = null;
        if(teamList != null){
            responseList = teamList.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        }
        return responseList;
    }

    // 팀 생성
    @Override
    @Transactional
    public TeamResponse createTeam(String name, Long accountId, Workspace workspace) {
        Team team = Team.builder()
                .name(name)
                .accountId(accountId)
                .workspace(workspace)
                .build();

        //팀장을 팀원으로 추가
        teamMemberService.addMember(team.getId(), accountId);
        TeamResponse teamResponse = convertToResponse(team);
        teamRepository.save(team);

        return teamResponse;
    }

    // 팀 삭제 (Soft Delete)
    @Override
    @Transactional
    public void deleteTeam(Long id) {
        Team team = getTeamByIdForUpdate(id);
        teamRepository.delete(team); //annotation 기반으로 Soft-Delete 처리된다.
    }

    // 팀 이름 변경
    @Override
    @Transactional
    public TeamResponse updateTeamName(Long id, String newName) {
        Team team = getTeamByIdForUpdate(id);
        team.setName(newName);
        teamRepository.save(team);

        TeamResponse response;
        response = convertToResponse(team);

        return response;
    }

    //팀 비밀번호 설정
    @Override
    @Transactional
    public TeamResponse updateTeamPassword(Long id, String newPassword) {
        Team team = getTeamByIdForUpdate(id);
        Long currentAccountId = authService.getCurrentUserId();
        
        // 팀장 권한 검사
        if (!team.getAccountId().equals(currentAccountId)) {
            throw new AccessDeniedException("팀 비밀번호는 팀장만 변경할 수 있습니다.");
        }
        
        // 비밀번호 유효성 검사
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("패스워드를 입력해 주세요.");
        }
        
        // 비밀번호 규칙 검사
        if (!isValidPassword(newPassword)) {
            throw new IllegalArgumentException(
                "비밀번호는 다음 규칙을 만족해야 합니다:\n" +
                "- 8자 이상\n" +
                "- 특수문자 1개 이상 포함(!@#$%^&*()_+-=[]{}|;:,.<>?)"
            );
        }
        
        team.setTeamPassword(passwordEncoder.encode(newPassword));

        TeamResponse response = convertToResponse(team);

        return response;
    }

    private boolean isValidPassword(String password) {
        // 최소 8자 이상
        if (password.length() < 8) {
            return false;
        }
        
        // 특수문자 포함 여부 검사
        String specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        boolean hasSpecialChar = false;
        
        for (char ch : password.toCharArray()) {
            if (specialChars.contains(String.valueOf(ch))) {
                hasSpecialChar = true;
                break;
            }
        }
        
        return hasSpecialChar;
    }

    @Override
    @Transactional
    public TeamResponse removeTeamPassword(Long id) {
        Team team = getTeamByIdForUpdate(id);
        Long currentAccountId = authService.getCurrentUserId();
        
        // 팀장 권한 검사
        if (!team.getAccountId().equals(currentAccountId)) {
            throw new AccessDeniedException("팀 비밀번호는 팀장만 삭제할 수 있습니다.");
        }
        
        team.setTeamPassword(null);
        TeamResponse response = convertToResponse(team);
        return response;
    }

    // 게시판 목록 가져오기
    @Override
    public List<?> getBoards(Long teamId) {
        return getTeamByIdForUpdate(teamId).getBoards(); // Board가 매핑되어 있으므로
    }

    // 칸반 목록 가져오기
    @Override
    public List<?> getKanbans(Long teamId) {
        return getTeamByIdForUpdate(teamId).getKanbans();
    }

    // 팀원 목록 가져오기
    @Override
    public List<?> getTeamMembers(Long teamId) {
        return getTeamByIdForUpdate(teamId).getTeamMembers();
    }

    private TeamResponse convertToResponse(Team team) {
        TeamResponse response = null;
        if (team != null) {
            response = TeamResponse.builder()
                    .id(team.getId())
                    .name(team.getName())
                    .ownerId(team.getAccountId())
                    .workspaceId(team.getWorkspace().getId())
                    .workspaceName(team.getWorkspace().getName()) // 있으면!
                    .build();
        }
        return response;
    }

}