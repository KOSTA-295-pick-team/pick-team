package com.pickteam.service.team;

import com.pickteam.domain.team.Team;
import com.pickteam.domain.team.TeamMember;
import com.pickteam.domain.user.Account;
import com.pickteam.domain.workspace.Workspace;
import com.pickteam.domain.workspace.WorkspaceMember;
import com.pickteam.dto.team.TeamCreateRequest;
import com.pickteam.dto.team.TeamMemberResponse;
import com.pickteam.dto.team.TeamResponse;
import com.pickteam.dto.team.TeamUpdateRequest;
import com.pickteam.repository.team.TeamMemberRepository;
import com.pickteam.repository.team.TeamRepository;
import com.pickteam.repository.user.AccountRepository;
import com.pickteam.repository.workspace.WorkspaceMemberRepository;
import com.pickteam.repository.workspace.WorkspaceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * 팀 서비스 단위 테스트
 * @ExtendWith(MockitoExtension.class)를 사용하여 Mock 객체들을 주입
 * 비즈니스 로직만 단위 테스트로 검증
 */
@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @InjectMocks
    private TeamService teamService;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Mock
    private AccountRepository accountRepository;

    @Test
    @DisplayName("팀을 생성할 수 있다")
    void createTeam_ValidRequest_ReturnsTeamResponse() {
        // Given
        Long accountId = 1L;
        TeamCreateRequest request = new TeamCreateRequest();
        request.setName("새 팀");
        request.setWorkspaceId(1L);

        Account account = createTestAccount();
        Workspace workspace = createTestWorkspace();
        Team team = createTestTeam();

        given(accountRepository.findById(accountId)).willReturn(Optional.of(account));
        given(workspaceRepository.findByIdAndIsDeletedFalse(1L)).willReturn(Optional.of(workspace));
        given(workspaceMemberRepository.existsActiveByWorkspaceIdAndAccountId(1L, accountId)).willReturn(true);
        given(teamRepository.save(any(Team.class))).willReturn(team);
        given(teamMemberRepository.save(any(TeamMember.class))).willReturn(createTestTeamMember());

        // When
        TeamResponse result = teamService.createTeam(accountId, request);

        // Then
        assertThat(result.getName()).isEqualTo("테스트 팀");
        verify(teamRepository).save(any(Team.class));
        verify(teamMemberRepository).save(any(TeamMember.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 팀 생성 시 예외가 발생한다")
    void createTeam_InvalidAccountId_ThrowsException() {
        // Given
        Long accountId = 999L;
        TeamCreateRequest request = new TeamCreateRequest();
        request.setName("새 팀");
        request.setWorkspaceId(1L);

        given(accountRepository.findById(accountId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> teamService.createTeam(accountId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("워크스페이스 멤버가 아닌 경우 팀 생성 시 예외가 발생한다")
    void createTeam_NotWorkspaceMember_ThrowsException() {
        // Given
        Long accountId = 1L;
        TeamCreateRequest request = new TeamCreateRequest();
        request.setName("새 팀");
        request.setWorkspaceId(1L);

        Account account = createTestAccount();
        Workspace workspace = createTestWorkspace();

        given(accountRepository.findById(accountId)).willReturn(Optional.of(account));
        given(workspaceRepository.findByIdAndIsDeletedFalse(1L)).willReturn(Optional.of(workspace));
        given(workspaceMemberRepository.existsActiveByWorkspaceIdAndAccountId(1L, accountId)).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> teamService.createTeam(accountId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("워크스페이스 멤버만 팀을 생성할 수 있습니다");
    }

    @Test
    @DisplayName("워크스페이스별 팀 목록을 조회할 수 있다")
    void getTeamsByWorkspace_ValidWorkspaceId_ReturnsTeamList() {
        // Given
        Long workspaceId = 1L;
        Long accountId = 1L;

        Team team = createTestTeam();
        given(workspaceMemberRepository.existsActiveByWorkspaceIdAndAccountId(workspaceId, accountId))
                .willReturn(true);
        given(teamRepository.findByWorkspaceId(workspaceId)).willReturn(List.of(team));

        // When
        List<TeamResponse> result = teamService.getTeamsByWorkspace(workspaceId, accountId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("테스트 팀");
        verify(teamRepository).findByWorkspaceId(workspaceId);
    }

    @Test
    @DisplayName("워크스페이스 접근 권한이 없는 경우 예외가 발생한다")
    void getTeamsByWorkspace_NoAccess_ThrowsException() {
        // Given
        Long workspaceId = 1L;
        Long accountId = 1L;

        given(workspaceMemberRepository.existsActiveByWorkspaceIdAndAccountId(workspaceId, accountId))
                .willReturn(false);

        // When & Then
        assertThatThrownBy(() -> teamService.getTeamsByWorkspace(workspaceId, accountId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("워크스페이스 접근 권한이 없습니다");
    }

    @Test
    @DisplayName("팀 상세 정보를 조회할 수 있다")
    void getTeam_ValidTeamId_ReturnsTeamResponse() {
        // Given
        Long teamId = 1L;
        Long accountId = 1L;

        Team team = createTestTeam();
        given(teamRepository.findByIdAndIsDeletedFalse(teamId)).willReturn(Optional.of(team));
        given(workspaceMemberRepository.existsActiveByWorkspaceIdAndAccountId(1L, accountId))
                .willReturn(true);

        // When
        TeamResponse result = teamService.getTeam(teamId, accountId);

        // Then
        assertThat(result.getName()).isEqualTo("테스트 팀");
        verify(teamRepository).findByIdAndIsDeletedFalse(teamId);
    }

    @Test
    @DisplayName("존재하지 않는 팀 조회 시 예외가 발생한다")
    void getTeam_NonExistentTeam_ThrowsException() {
        // Given
        Long teamId = 999L;
        Long accountId = 1L;

        given(teamRepository.findByIdAndIsDeletedFalse(teamId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> teamService.getTeam(teamId, accountId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("팀을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("팀을 수정할 수 있다")
    void updateTeam_ValidRequest_ReturnsUpdatedTeamResponse() {
        // Given
        Long teamId = 1L;
        Long accountId = 1L;
        TeamUpdateRequest request = new TeamUpdateRequest();
        request.setName("수정된 팀명");

        Team team = createTestTeam();
        given(teamRepository.findByIdAndIsDeletedFalse(teamId)).willReturn(Optional.of(team));
        given(teamMemberRepository.isTeamLeader(teamId, accountId)).willReturn(true);
        given(teamRepository.save(any(Team.class))).willReturn(team);

        // When
        TeamResponse result = teamService.updateTeam(teamId, accountId, request);

        // Then
        assertThat(result.getName()).isEqualTo("수정된 팀명");
        verify(teamRepository).save(any(Team.class));
    }

    @Test
    @DisplayName("팀장이 아닌 경우 팀 수정 시 예외가 발생한다")
    void updateTeam_NotTeamLeader_ThrowsException() {
        // Given
        Long teamId = 1L;
        Long accountId = 1L;
        TeamUpdateRequest request = new TeamUpdateRequest();
        request.setName("수정된 팀명");

        Team team = createTestTeam();
        given(teamRepository.findByIdAndIsDeletedFalse(teamId)).willReturn(Optional.of(team));
        given(teamMemberRepository.isTeamLeader(teamId, accountId)).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> teamService.updateTeam(teamId, accountId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("팀 수정 권한이 없습니다");
    }

    @Test
    @DisplayName("팀에 참여할 수 있다")
    void joinTeam_ValidRequest_JoinsSuccessfully() {
        // Given
        Long teamId = 1L;
        Long accountId = 1L;

        Account account = createTestAccount();
        Team team = createTestTeam();

        given(accountRepository.findById(accountId)).willReturn(Optional.of(account));
        given(teamRepository.findByIdAndIsDeletedFalse(teamId)).willReturn(Optional.of(team));
        given(workspaceMemberRepository.existsActiveByWorkspaceIdAndAccountId(1L, accountId))
                .willReturn(true);
        given(teamMemberRepository.existsByTeamIdAndAccountId(teamId, accountId)).willReturn(false);
        given(teamMemberRepository.findByTeamIdAndAccountId(teamId, accountId))
                .willReturn(Optional.empty());
        given(teamMemberRepository.save(any(TeamMember.class))).willReturn(createTestTeamMember());

        // When
        teamService.joinTeam(teamId, accountId);

        // Then
        verify(teamMemberRepository).save(any(TeamMember.class));
    }

    @Test
    @DisplayName("이미 팀 멤버인 경우 참여 시 예외가 발생한다")
    void joinTeam_AlreadyMember_ThrowsException() {
        // Given
        Long teamId = 1L;
        Long accountId = 1L;

        Account account = createTestAccount();
        Team team = createTestTeam();

        given(accountRepository.findById(accountId)).willReturn(Optional.of(account));
        given(teamRepository.findByIdAndIsDeletedFalse(teamId)).willReturn(Optional.of(team));
        given(workspaceMemberRepository.existsActiveByWorkspaceIdAndAccountId(1L, accountId))
                .willReturn(true);
        given(teamMemberRepository.existsByTeamIdAndAccountId(teamId, accountId)).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> teamService.joinTeam(teamId, accountId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("이미 팀의 멤버입니다");
    }

    private Account createTestAccount() {
        return Account.builder()
                .id(1L)
                .email("test@example.com")
                .name("테스트 사용자")
                .build();
    }

    private Workspace createTestWorkspace() {
        return Workspace.builder()
                .id(1L)
                .name("테스트 워크스페이스")
                .build();
    }

    private Team createTestTeam() {
        return Team.builder()
                .id(1L)
                .name("테스트 팀")
                .workspace(createTestWorkspace())
                .build();
    }

    private TeamMember createTestTeamMember() {
        return TeamMember.builder()
                .id(1L)
                .team(createTestTeam())
                .account(createTestAccount())
                .teamRole(TeamMember.TeamRole.LEADER)
                .teamStatus(TeamMember.TeamStatus.ACTIVE)
                .build();
    }
}
