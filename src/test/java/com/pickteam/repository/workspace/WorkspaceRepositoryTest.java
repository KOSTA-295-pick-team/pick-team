package com.pickteam.repository.workspace;

import com.pickteam.config.QueryDslConfig;
import com.pickteam.domain.workspace.Workspace;
import com.pickteam.domain.user.Account;
import com.pickteam.config.TestQueryDslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 워크스페이스 리포지토리 단위 테스트
 * @DataJpaTest를 사용하여 JPA 관련 기능만 테스트
 */
@DataJpaTest(includeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {WorkspaceRepository.class}
))
@Import(TestQueryDslConfig.class)
@ActiveProfiles("test")
class WorkspaceRepositoryTest {

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("워크스페이스 저장 시 정상적으로 저장된다")
    void save_ValidWorkspace_SavesSuccessfully() {
        // given
        Account account = Account.builder()
                .email("test@example.com")
                .name("테스트사용자")
                .password("password")
                .build();
        entityManager.persistAndFlush(account);

        Workspace workspace = Workspace.builder()
                .name("테스트 워크스페이스")
                .iconUrl("🏢")
                .account(account)
                .password("encodedPassword")
                .url("invite123")
                .build();

        // when
        Workspace savedWorkspace = workspaceRepository.save(workspace);

        // then
        assertThat(savedWorkspace.getId()).isNotNull();
        assertThat(savedWorkspace.getName()).isEqualTo("테스트 워크스페이스");
        assertThat(savedWorkspace.getIconUrl()).isEqualTo("🏢");
        assertThat(savedWorkspace.getAccount().getId()).isEqualTo(account.getId());
        assertThat(savedWorkspace.getUrl()).isEqualTo("invite123");
    }

    @Test
    @DisplayName("ID로 워크스페이스 조회 시 정상적으로 조회된다")
    void findById_ValidId_ReturnsWorkspace() {
        // given
        Account account = Account.builder()
                .email("test@example.com")
                .name("테스트사용자")
                .password("password")
                .build();
        entityManager.persistAndFlush(account);

        Workspace workspace = Workspace.builder()
                .name("테스트 워크스페이스")
                .account(account)
                .url("invite123")
                .build();
        entityManager.persistAndFlush(workspace);

        // when
        Optional<Workspace> found = workspaceRepository.findById(workspace.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("테스트 워크스페이스");
        assertThat(found.get().getUrl()).isEqualTo("invite123");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 워크스페이스 조회 시 빈 Optional을 반환한다")
    void findById_NonExistentId_ReturnsEmptyOptional() {
        // given
        Long nonExistentId = 999L;

        // when
        Optional<Workspace> found = workspaceRepository.findById(nonExistentId);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("초대 URL로 워크스페이스 조회 시 정상적으로 조회된다")
    void findByUrl_ValidUrl_ReturnsWorkspace() {
        // given
        Account account = Account.builder()
                .email("test@example.com")
                .name("테스트사용자")
                .password("password")
                .build();
        entityManager.persistAndFlush(account);

        Workspace workspace = Workspace.builder()
                .name("테스트 워크스페이스")
                .account(account)
                .url("unique-invite-code")
                .build();
        entityManager.persistAndFlush(workspace);

        // when
        Optional<Workspace> found = workspaceRepository.findByUrl("unique-invite-code");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("테스트 워크스페이스");
        assertThat(found.get().getUrl()).isEqualTo("unique-invite-code");
    }

    @Test
    @DisplayName("존재하지 않는 URL로 워크스페이스 조회 시 빈 Optional을 반환한다")
    void findByUrl_NonExistentUrl_ReturnsEmptyOptional() {
        // given
        String nonExistentUrl = "non-existent-code";

        // when
        Optional<Workspace> found = workspaceRepository.findByUrl(nonExistentUrl);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("워크스페이스 삭제 시 정상적으로 삭제된다")
    void delete_ValidWorkspace_DeletesSuccessfully() {
        // given
        Account account = Account.builder()
                .email("test@example.com")
                .name("테스트사용자")
                .password("password")
                .build();
        entityManager.persistAndFlush(account);

        Workspace workspace = Workspace.builder()
                .name("삭제할 워크스페이스")
                .account(account)
                .url("delete-me")
                .build();
        entityManager.persistAndFlush(workspace);
        
        Long workspaceId = workspace.getId();

        // when
        workspaceRepository.delete(workspace);
        entityManager.flush();

        // then
        Optional<Workspace> found = workspaceRepository.findById(workspaceId);
        assertThat(found).isEmpty();
    }
}
