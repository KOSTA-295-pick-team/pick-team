/**
 * 단위 테스트 재작성 예정
 */

//package com.pickteam.repository.kanban;
//
//import com.pickteam.config.TestQueryDslConfig;
//import com.pickteam.domain.enums.UserRole;
//import com.pickteam.domain.kanban.Kanban;
//import com.pickteam.domain.team.Team;
//import com.pickteam.domain.user.Account;
//import com.pickteam.domain.workspace.Workspace;
//import com.pickteam.repository.team.TeamRepository;
//import com.pickteam.repository.user.AccountRepository;
//import com.pickteam.repository.workspace.WorkspaceRepository;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.*;
//
///**
// * 칸반 리포지토리 테스트
// * @DataJpaTest를 사용하여 JPA Repository 테스트에 집중
// */
//@DataJpaTest
//@Transactional
//@Import(TestQueryDslConfig.class)
//class KanbanRepositoryTest {
//
//    @Autowired
//    private KanbanRepository kanbanRepository;
//
//    @Autowired
//    private TeamRepository teamRepository;
//
//    @Autowired
//    private WorkspaceRepository workspaceRepository;
//
//    @Autowired
//    private AccountRepository accountRepository;
//
//    @Test
//    @DisplayName("팀 ID로 칸반 보드를 조회할 수 있다")
//    void findByTeamId_ExistingTeam_ReturnsKanban() {
//        // Given
//        Account owner = createTestAccount("owner@test.com");
//        Workspace workspace = createTestWorkspace("테스트 워크스페이스", owner);
//        Team team = createTestTeam("테스트 팀", workspace, owner);
//        Kanban kanban = createTestKanban(team, workspace);
//
//        // When
//        Optional<Kanban> result = kanbanRepository.findByTeamId(team.getId());
//
//        // Then
//        assertThat(result).isPresent();
//        assertThat(result.get().getId()).isEqualTo(kanban.getId());
//        assertThat(result.get().getTeam().getId()).isEqualTo(team.getId());
//        assertThat(result.get().getWorkspace().getId()).isEqualTo(workspace.getId());
//    }
//
//    @Test
//    @DisplayName("존재하지 않는 팀 ID로 조회 시 빈 결과를 반환한다")
//    void findByTeamId_NonExistingTeam_ReturnsEmpty() {
//        // Given
//        Long nonExistingTeamId = 999L;
//
//        // When
//        Optional<Kanban> result = kanbanRepository.findByTeamId(nonExistingTeamId);
//
//        // Then
//        assertThat(result).isEmpty();
//    }
//
//    @Test
//    @DisplayName("워크스페이스 ID로 칸반 보드 목록을 조회할 수 있다")
//    void findByWorkspaceId_ExistingWorkspace_ReturnsKanbans() {
//        // Given
//        Account owner = createTestAccount("owner@test.com");
//        Workspace workspace = createTestWorkspace("테스트 워크스페이스", owner);
//        Team team1 = createTestTeam("팀 1", workspace, owner);
//        Team team2 = createTestTeam("팀 2", workspace, owner);
//        Kanban kanban1 = createTestKanban(team1, workspace);
//        Kanban kanban2 = createTestKanban(team2, workspace);
//
//        // When
//        List<Kanban> result = kanbanRepository.findByWorkspaceId(workspace.getId());
//
//        // Then
//        assertThat(result).hasSize(2);
//        assertThat(result).extracting(Kanban::getId)
//                .containsExactlyInAnyOrder(kanban1.getId(), kanban2.getId());
//    }
//
//    @Test
//    @DisplayName("팀 ID와 워크스페이스 ID로 칸반 보드를 조회할 수 있다")
//    void findByTeamIdAndWorkspaceId_ExistingTeamAndWorkspace_ReturnsKanban() {
//        // Given
//        Account owner = createTestAccount("owner@test.com");
//        Workspace workspace = createTestWorkspace("테스트 워크스페이스", owner);
//        Team team = createTestTeam("테스트 팀", workspace, owner);
//        Kanban kanban = createTestKanban(team, workspace);
//
//        // When
//        Optional<Kanban> result = kanbanRepository.findByTeamIdAndWorkspaceId(
//                team.getId(), workspace.getId());
//
//        // Then
//        assertThat(result).isPresent();
//        assertThat(result.get().getId()).isEqualTo(kanban.getId());
//        assertThat(result.get().getTeam().getId()).isEqualTo(team.getId());
//        assertThat(result.get().getWorkspace().getId()).isEqualTo(workspace.getId());
//    }
//
//    @Test
//    @DisplayName("삭제된 칸반 보드는 조회되지 않는다")
//    void findByTeamId_DeletedKanban_ReturnsEmpty() {
//        // Given
//        Account owner = createTestAccount("owner@test.com");
//        Workspace workspace = createTestWorkspace("테스트 워크스페이스", owner);
//        Team team = createTestTeam("테스트 팀", workspace, owner);
//        Kanban kanban = createTestKanban(team, workspace);
//        
//        // 칸반 보드 삭제
//        kanban.markDeleted();
//        kanbanRepository.save(kanban);
//
//        // When
//        Optional<Kanban> result = kanbanRepository.findByTeamId(team.getId());
//
//        // Then
//        assertThat(result).isEmpty();
//    }
//
//    @Test
//    @DisplayName("삭제된 칸반 보드는 워크스페이스 조회에서 제외된다")
//    void findByWorkspaceId_DeletedKanban_ExcludesDeleted() {
//        // Given
//        Account owner = createTestAccount("owner@test.com");
//        Workspace workspace = createTestWorkspace("테스트 워크스페이스", owner);
//        Team team1 = createTestTeam("팀 1", workspace, owner);
//        Team team2 = createTestTeam("팀 2", workspace, owner);
//        Kanban kanban1 = createTestKanban(team1, workspace);
//        Kanban kanban2 = createTestKanban(team2, workspace);
//        
//        // 하나의 칸반 보드 삭제
//        kanban1.markDeleted();
//        kanbanRepository.save(kanban1);
//
//        // When
//        List<Kanban> result = kanbanRepository.findByWorkspaceId(workspace.getId());
//
//        // Then
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).getId()).isEqualTo(kanban2.getId());
//    }
//
//    @Test
//    @DisplayName("칸반 보드를 저장할 수 있다")
//    void save_ValidKanban_ReturnsKanban() {
//        // Given
//        Account owner = createTestAccount("owner@test.com");
//        Workspace workspace = createTestWorkspace("테스트 워크스페이스", owner);
//        Team team = createTestTeam("테스트 팀", workspace, owner);
//
//        Kanban kanban = Kanban.builder()
//                .team(team)
//                .workspace(workspace)
//                .build();
//
//        // When
//        Kanban savedKanban = kanbanRepository.save(kanban);
//
//        // Then
//        assertThat(savedKanban.getId()).isNotNull();
//        assertThat(savedKanban.getTeam().getId()).isEqualTo(team.getId());
//        assertThat(savedKanban.getWorkspace().getId()).isEqualTo(workspace.getId());
//        assertThat(savedKanban.getCreatedAt()).isNotNull();
//        assertThat(savedKanban.getUpdatedAt()).isNotNull();
//        assertThat(savedKanban.getIsDeleted()).isFalse();
//    }
//
//    @Test
//    @DisplayName("칸반 보드를 ID로 조회할 수 있다")
//    void findById_ExistingKanban_ReturnsKanban() {
//        // Given
//        Account owner = createTestAccount("owner@test.com");
//        Workspace workspace = createTestWorkspace("테스트 워크스페이스", owner);
//        Team team = createTestTeam("테스트 팀", workspace, owner);
//        Kanban kanban = createTestKanban(team, workspace);
//
//        // When
//        Optional<Kanban> result = kanbanRepository.findById(kanban.getId());
//
//        // Then
//        assertThat(result).isPresent();
//        assertThat(result.get().getId()).isEqualTo(kanban.getId());
//        assertThat(result.get().getTeam().getId()).isEqualTo(team.getId());
//        assertThat(result.get().getWorkspace().getId()).isEqualTo(workspace.getId());
//    }
//
//    @Test
//    @DisplayName("존재하지 않는 ID로 조회 시 빈 결과를 반환한다")
//    void findById_NonExistingId_ReturnsEmpty() {
//        // Given
//        Long nonExistingId = 999L;
//
//        // When
//        Optional<Kanban> result = kanbanRepository.findById(nonExistingId);
//
//        // Then
//        assertThat(result).isEmpty();
//    }
//
//    private Account createTestAccount(String email) {
//        Account account = Account.builder()
//                .email(email)
//                .password("password123")
//                .name("테스트 사용자")
//                .role(UserRole.USER)
//                .build();
//        return accountRepository.save(account);
//    }
//
//    private Workspace createTestWorkspace(String name, Account owner) {
//        Workspace workspace = Workspace.builder()
//                .name(name)
//                .account(owner)
//                .build();
//        return workspaceRepository.save(workspace);
//    }
//
//    private Team createTestTeam(String name, Workspace workspace, Account leader) {
//        Team team = Team.builder()
//                .name(name)
//                .workspace(workspace)
//                .build();
//        return teamRepository.save(team);
//    }
//
//    private Kanban createTestKanban(Team team, Workspace workspace) {
//        Kanban kanban = Kanban.builder()
//                .team(team)
//                .workspace(workspace)
//                .build();
//        return kanbanRepository.save(kanban);
//    }
//}
