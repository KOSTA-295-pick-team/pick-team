package com.pickteam.repository.team;

import com.pickteam.config.TestQueryDslConfig;
import com.pickteam.domain.team.Team;
import com.pickteam.domain.user.Account;
import com.pickteam.domain.workspace.Workspace;
import com.pickteam.repository.user.AccountRepository;
import com.pickteam.repository.workspace.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 팀 리포지토리 테스트
 * @DataJpaTest를 사용하여 JPA 관련 설정만 로드
 */
@DataJpaTest
@Import(TestQueryDslConfig.class)
@ActiveProfiles("test")
class TeamRepositoryTest {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private AccountRepository accountRepository;

    private Workspace testWorkspace;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        // 테스트용 계정 생성
        testAccount = Account.builder()
                .email("test@example.com")
                .password("password")
                .name("테스트 사용자")
                .build();
        testAccount = accountRepository.save(testAccount);

        // 테스트용 워크스페이스 생성
        testWorkspace = Workspace.builder()
                .name("테스트 워크스페이스")
                .account(testAccount)
                .build();
        testWorkspace = workspaceRepository.save(testWorkspace);
    }

    @Test
    @DisplayName("워크스페이스 ID로 활성 팀 목록을 조회할 수 있다")
    void findByWorkspaceId_ValidWorkspaceId_ReturnsActiveTeams() {
        // Given
        Team team1 = Team.builder()
                .name("팀 1")
                .workspace(testWorkspace)
                .build();

        Team team2 = Team.builder()
                .name("팀 2")
                .workspace(testWorkspace)
                .build();

        teamRepository.save(team1);
        teamRepository.save(team2);

        // When
        List<Team> result = teamRepository.findByWorkspaceId(testWorkspace.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Team::getName).containsExactlyInAnyOrder("팀 1", "팀 2");
        assertThat(result).allMatch(team -> team.getWorkspace().getId().equals(testWorkspace.getId()));
    }

    @Test
    @DisplayName("삭제된 팀은 워크스페이스 조회에서 제외된다")
    void findByWorkspaceId_DeletedTeam_ExcludesDeletedTeams() {
        // Given
        Team activeTeam = Team.builder()
                .name("활성 팀")
                .workspace(testWorkspace)
                .build();

        Team deletedTeam = Team.builder()
                .name("삭제된 팀")
                .workspace(testWorkspace)
                .build();
        deletedTeam.markDeleted(); // 소프트 삭제

        teamRepository.save(activeTeam);
        teamRepository.save(deletedTeam);

        // When
        List<Team> result = teamRepository.findByWorkspaceId(testWorkspace.getId());

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("활성 팀");
    }

    @Test
    @DisplayName("ID로 활성 팀을 조회할 수 있다")
    void findByIdAndIsDeletedFalse_ValidId_ReturnsTeam() {
        // Given
        Team team = Team.builder()
                .name("테스트 팀")
                .workspace(testWorkspace)
                .build();
        Team savedTeam = teamRepository.save(team);

        // When
        Optional<Team> result = teamRepository.findByIdAndIsDeletedFalse(savedTeam.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("테스트 팀");
        assertThat(result.get().getWorkspace().getId()).isEqualTo(testWorkspace.getId());
    }

    @Test
    @DisplayName("삭제된 팀은 ID로 조회되지 않는다")
    void findByIdAndIsDeletedFalse_DeletedTeam_ReturnsEmpty() {
        // Given
        Team team = Team.builder()
                .name("삭제된 팀")
                .workspace(testWorkspace)
                .build();
        team.markDeleted(); // 소프트 삭제
        Team savedTeam = teamRepository.save(team);

        // When
        Optional<Team> result = teamRepository.findByIdAndIsDeletedFalse(savedTeam.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회하면 빈 결과를 반환한다")
    void findByIdAndIsDeletedFalse_NonExistentId_ReturnsEmpty() {
        // Given
        Long nonExistentId = 999L;

        // When
        Optional<Team> result = teamRepository.findByIdAndIsDeletedFalse(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("다른 워크스페이스의 팀은 조회되지 않는다")
    void findByWorkspaceId_DifferentWorkspace_ReturnsEmpty() {
        // Given
        // 다른 워크스페이스 생성
        Workspace otherWorkspace = Workspace.builder()
                .name("다른 워크스페이스")
                .account(testAccount)
                .build();
        otherWorkspace = workspaceRepository.save(otherWorkspace);

        // 다른 워크스페이스에 팀 생성
        Team team = Team.builder()
                .name("다른 워크스페이스 팀")
                .workspace(otherWorkspace)
                .build();
        teamRepository.save(team);

        // When
        List<Team> result = teamRepository.findByWorkspaceId(testWorkspace.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("팀이 없는 워크스페이스는 빈 목록을 반환한다")
    void findByWorkspaceId_EmptyWorkspace_ReturnsEmptyList() {
        // Given
        // 팀이 없는 새로운 워크스페이스
        Workspace emptyWorkspace = Workspace.builder()
                .name("빈 워크스페이스")
                .account(testAccount)
                .build();
        emptyWorkspace = workspaceRepository.save(emptyWorkspace);

        // When
        List<Team> result = teamRepository.findByWorkspaceId(emptyWorkspace.getId());

        // Then
        assertThat(result).isEmpty();
    }
}
