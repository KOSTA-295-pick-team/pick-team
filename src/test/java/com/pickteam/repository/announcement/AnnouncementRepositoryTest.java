package com.pickteam.repository.announcement;

import com.pickteam.config.TestQueryDslConfig;
import com.pickteam.domain.announcement.Announcement;
import com.pickteam.domain.enums.UserRole;
import com.pickteam.domain.team.Team;
import com.pickteam.domain.user.Account;
import com.pickteam.domain.workspace.Workspace;
import com.pickteam.repository.team.TeamRepository;
import com.pickteam.repository.user.AccountRepository;
import com.pickteam.repository.workspace.WorkspaceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * 공지사항 리포지토리 테스트
 * @DataJpaTest를 사용하여 JPA Repository 테스트에 집중
 */
@DataJpaTest
@Transactional
@Import(TestQueryDslConfig.class)
class AnnouncementRepositoryTest {

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Test
    @DisplayName("삭제되지 않은 공지사항을 ID로 조회할 수 있다")
    void findByIdAndIsDeletedFalse_ExistingId_ReturnsAnnouncement() {
        // given
        Workspace workspace = createAndSaveWorkspace("테스트 워크스페이스");
        Account account = createAndSaveAccount("test@example.com", "홍길동");
        Team team = createAndSaveTeam("개발팀", workspace);
        Announcement announcement = createAndSaveAnnouncement("공지사항 제목", "공지사항 내용", account, team);

        // when
        Optional<Announcement> result = announcementRepository.findByIdAndIsDeletedFalse(announcement.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("공지사항 제목");
        assertThat(result.get().getContent()).isEqualTo("공지사항 내용");
        assertThat(result.get().getAccount().getName()).isEqualTo("홍길동");
        assertThat(result.get().getTeam().getName()).isEqualTo("개발팀");
        assertThat(result.get().getIsDeleted()).isFalse();
    }

    @Test
    @DisplayName("삭제된 공지사항은 ID로 조회할 수 없다")
    void findByIdAndIsDeletedFalse_DeletedAnnouncement_ReturnsEmpty() {
        // given
        Workspace workspace = createAndSaveWorkspace("테스트 워크스페이스");
        Account account = createAndSaveAccount("test@example.com", "홍길동");
        Team team = createAndSaveTeam("개발팀", workspace);
        Announcement announcement = createAndSaveAnnouncement("공지사항 제목", "공지사항 내용", account, team);
        
        // 공지사항 삭제 처리
        announcement.markDeleted();
        announcementRepository.save(announcement);

        // when
        Optional<Announcement> result = announcementRepository.findByIdAndIsDeletedFalse(announcement.getId());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회하면 빈 결과를 반환한다")
    void findByIdAndIsDeletedFalse_NonExistentId_ReturnsEmpty() {
        // given
        Long nonExistentId = 999L;

        // when
        Optional<Announcement> result = announcementRepository.findByIdAndIsDeletedFalse(nonExistentId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("계정이 작성한 삭제되지 않은 공지사항을 생성 시간 역순으로 조회할 수 있다")
    void findByAccountIdAndIsDeletedFalseOrderByCreatedAtDesc_ReturnsAnnouncementsInDescOrder() {
        // given
        Workspace workspace = createAndSaveWorkspace("테스트 워크스페이스");
        Account account = createAndSaveAccount("test@example.com", "홍길동");
        Team team = createAndSaveTeam("개발팀", workspace);
        
        // 3개의 공지사항 생성 (시간 차이를 두기 위해)
        createAndSaveAnnouncement("첫 번째 공지", "내용1", account, team);
        createAndSaveAnnouncement("두 번째 공지", "내용2", account, team);
        createAndSaveAnnouncement("세 번째 공지", "내용3", account, team);

        // when
        List<Announcement> results = announcementRepository
                .findByAccountIdAndIsDeletedFalseOrderByCreatedAtDesc(account.getId());

        // then
        assertThat(results).hasSize(3);
        assertThat(results.get(0).getTitle()).isEqualTo("세 번째 공지");
        assertThat(results.get(1).getTitle()).isEqualTo("두 번째 공지");
        assertThat(results.get(2).getTitle()).isEqualTo("첫 번째 공지");
        
        // 모두 같은 계정이 작성했는지 확인
        assertThat(results).allMatch(announcement -> 
                announcement.getAccount().getId().equals(account.getId()));
    }

    @Test
    @DisplayName("워크스페이스 내 삭제되지 않은 공지사항 개수를 조회할 수 있다")
    void countByWorkspaceIdAndIsDeletedFalse_ReturnsCorrectCount() {
        // given
        Workspace workspace1 = createAndSaveWorkspace("워크스페이스1");
        Workspace workspace2 = createAndSaveWorkspace("워크스페이스2");
        Account account = createAndSaveAccount("test@example.com", "홍길동");
        Team team1 = createAndSaveTeam("팀1", workspace1);
        Team team2 = createAndSaveTeam("팀2", workspace2);
        
        // workspace1에 공지사항 2개 생성
        createAndSaveAnnouncement("공지1", "내용1", account, team1);
        createAndSaveAnnouncement("공지2", "내용2", account, team1);
        
        // workspace2에 공지사항 1개 생성
        createAndSaveAnnouncement("공지3", "내용3", account, team2);

        // when
        Long countWorkspace1 = announcementRepository.countByWorkspaceIdAndIsDeletedFalse(workspace1.getId());
        Long countWorkspace2 = announcementRepository.countByWorkspaceIdAndIsDeletedFalse(workspace2.getId());

        // then
        assertThat(countWorkspace1).isEqualTo(2L);
        assertThat(countWorkspace2).isEqualTo(1L);
    }

    @Test
    @DisplayName("워크스페이스 내 팀별 삭제되지 않은 공지사항을 페이징 조회할 수 있다")
    void findByTeamIdAndWorkspaceIdAndIsDeletedFalse_WithPaging_ReturnsPagedResults() {
        // given
        Workspace workspace = createAndSaveWorkspace("테스트 워크스페이스");
        Account account = createAndSaveAccount("test@example.com", "홍길동");
        Team team = createAndSaveTeam("개발팀", workspace);
        
        // 5개의 공지사항 생성
        for (int i = 1; i <= 5; i++) {
            createAndSaveAnnouncement("공지사항 " + i, "내용 " + i, account, team);
        }

        Pageable pageable = PageRequest.of(0, 3); // 첫 번째 페이지, 3개씩

        // when
        Page<Announcement> result = announcementRepository
                .findByTeamIdAndIsDeletedFalse(team.getId(), pageable);

        // then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(5L);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.hasNext()).isTrue();
    }

    private Workspace createAndSaveWorkspace(String name) {
        String uniqueEmail = "workspace_" + System.nanoTime() + "@example.com";
        Account account = Account.builder()
                .email(uniqueEmail)
                .password("encoded-password")
                .role(UserRole.USER)
                .build();

        accountRepository.save(account);

        Workspace workspace = Workspace.builder()
                .name(name)
                .url("url-" + System.nanoTime()) // URL도 고유화
                .account(account)
                .build();

        return workspaceRepository.save(workspace);
    }


    private Account createAndSaveAccount(String email, String name) {
        Account account = Account.builder()
                .email(email)
                .password("password")
                .name(name)
                .age(25)
                .build();
        return accountRepository.save(account);
    }

    private Team createAndSaveTeam(String name, Workspace workspace) {
        Team team = Team.builder()
                .name(name)
                .workspace(workspace)
                .build();
        return teamRepository.save(team);
    }

    private Announcement createAndSaveAnnouncement(String title, String content, Account account, Team team) {
        Announcement announcement = Announcement.builder()
                .title(title)
                .content(content)
                .account(account)
                .team(team)
                .build();
        return announcementRepository.save(announcement);
    }
}
