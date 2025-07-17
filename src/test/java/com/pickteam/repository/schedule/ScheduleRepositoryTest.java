package com.pickteam.repository.schedule;

import com.pickteam.config.TestQueryDslConfig;
import com.pickteam.domain.schedule.Schedule;
import com.pickteam.domain.schedule.ScheduleType;
import com.pickteam.domain.team.Team;
import com.pickteam.domain.user.Account;
import com.pickteam.domain.workspace.Workspace;
import com.pickteam.repository.team.TeamRepository;
import com.pickteam.repository.user.AccountRepository;
import com.pickteam.repository.workspace.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 일정 리포지토리 테스트
 * @DataJpaTest를 사용하여 JPA 관련 설정만 로드
 */
@DataJpaTest
@Import(TestQueryDslConfig.class)
@ActiveProfiles("test")
class ScheduleRepositoryTest {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    private Account testAccount;
    private Team testTeam;
    private Workspace testWorkspace;

    @BeforeEach
    void setUp() {
        // 테스트용 계정 생성 (워크스페이스 생성자로 사용)
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

        // 테스트용 팀 생성
        testTeam = Team.builder()
                .name("테스트 팀")
                .workspace(testWorkspace)
                .build();
        testTeam = teamRepository.save(testTeam);
    }

    @Test
    @DisplayName("팀 ID로 활성 일정을 조회할 수 있다")
    void findByTeamIdWithDetailsAndIsDeletedFalse_ValidTeamId_ReturnsSchedules() {
        // Given
        Schedule schedule = Schedule.builder()
                .title("팀 회의")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusHours(2))
                .scheduleDesc("정기 팀 회의")
                .type(ScheduleType.MEETING)
                .account(testAccount)
                .team(testTeam)
                .build();
        scheduleRepository.save(schedule);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Schedule> result = scheduleRepository.findByTeamIdWithDetailsAndIsDeletedFalse(testTeam.getId(), pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("팀 회의");
        assertThat(result.getContent().get(0).getTeam().getId()).isEqualTo(testTeam.getId());
        assertThat(result.getContent().get(0).getAccount().getId()).isEqualTo(testAccount.getId());
    }

    @Test
    @DisplayName("삭제된 일정은 조회되지 않는다")
    void findByTeamIdWithDetailsAndIsDeletedFalse_DeletedSchedule_ReturnsEmpty() {
        // Given
        Schedule schedule = Schedule.builder()
                .title("삭제된 회의")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusHours(2))
                .scheduleDesc("삭제될 회의")
                .type(ScheduleType.MEETING)
                .account(testAccount)
                .team(testTeam)
                .build();
        schedule.markDeleted(); // 소프트 삭제
        scheduleRepository.save(schedule);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Schedule> result = scheduleRepository.findByTeamIdWithDetailsAndIsDeletedFalse(testTeam.getId(), pageable);

        // Then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("기간별로 활성 일정을 조회할 수 있다")
    void findByTeamIdAndDateRangeAndIsDeletedFalse_ValidDateRange_ReturnsSchedules() {
        // Given
        LocalDateTime baseTime = LocalDateTime.now();
        LocalDateTime startDate = baseTime.minusDays(1);
        LocalDateTime endDate = baseTime.plusDays(1);

        Schedule schedule1 = Schedule.builder()
                .title("기간 내 회의")
                .startDate(baseTime)
                .endDate(baseTime.plusHours(2))
                .scheduleDesc("기간 내 회의")
                .type(ScheduleType.MEETING)
                .account(testAccount)
                .team(testTeam)
                .build();

        Schedule schedule2 = Schedule.builder()
                .title("기간 외 회의")
                .startDate(baseTime.plusDays(5))
                .endDate(baseTime.plusDays(5).plusHours(2))
                .scheduleDesc("기간 외 회의")
                .type(ScheduleType.MEETING)
                .account(testAccount)
                .team(testTeam)
                .build();

        scheduleRepository.save(schedule1);
        scheduleRepository.save(schedule2);

        // When
        List<Schedule> result = scheduleRepository.findByTeamIdAndDateRangeAndIsDeletedFalse(
                testTeam.getId(), startDate, endDate);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("기간 내 회의");
    }

    @Test
    @DisplayName("일정 타입별로 활성 일정을 조회할 수 있다")
    void findByTeamIdAndTypeAndIsDeletedFalse_ValidType_ReturnsSchedules() {
        // Given
        Schedule meetingSchedule = Schedule.builder()
                .title("회의")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusHours(2))
                .scheduleDesc("회의 일정")
                .type(ScheduleType.MEETING)
                .account(testAccount)
                .team(testTeam)
                .build();

        Schedule deadlineSchedule = Schedule.builder()
                .title("마감일")
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(1).plusHours(1))
                .scheduleDesc("마감일 일정")
                .type(ScheduleType.DEADLINE)
                .account(testAccount)
                .team(testTeam)
                .build();

        scheduleRepository.save(meetingSchedule);
        scheduleRepository.save(deadlineSchedule);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Schedule> result = scheduleRepository.findByTeamIdAndTypeAndIsDeletedFalse(
                testTeam.getId(), ScheduleType.MEETING, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getType()).isEqualTo(ScheduleType.MEETING);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("회의");
    }

    @Test
    @DisplayName("사용자별로 활성 일정을 조회할 수 있다")
    void findByAccountIdAndIsDeletedFalse_ValidAccountId_ReturnsSchedules() {
        // Given
        Schedule schedule = Schedule.builder()
                .title("개인 일정")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusHours(2))
                .scheduleDesc("개인 일정")
                .type(ScheduleType.OTHER)
                .account(testAccount)
                .team(testTeam)
                .build();
        scheduleRepository.save(schedule);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Schedule> result = scheduleRepository.findByAccountIdAndIsDeletedFalse(testAccount.getId(), pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAccount().getId()).isEqualTo(testAccount.getId());
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("개인 일정");
    }

    @Test
    @DisplayName("ID로 활성 일정을 상세 조회할 수 있다")
    void findByIdWithDetailsAndIsDeletedFalse_ValidId_ReturnsSchedule() {
        // Given
        Schedule schedule = Schedule.builder()
                .title("상세 조회 일정")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusHours(2))
                .scheduleDesc("상세 조회용 일정")
                .type(ScheduleType.WORKSHOP)
                .account(testAccount)
                .team(testTeam)
                .build();
        Schedule savedSchedule = scheduleRepository.save(schedule);

        // When
        Optional<Schedule> result = scheduleRepository.findByIdWithDetailsAndIsDeletedFalse(savedSchedule.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("상세 조회 일정");
        assertThat(result.get().getAccount().getName()).isEqualTo("테스트 사용자");
        assertThat(result.get().getTeam().getName()).isEqualTo("테스트 팀");
    }

    @Test
    @DisplayName("삭제된 일정은 ID로 조회되지 않는다")
    void findByIdWithDetailsAndIsDeletedFalse_DeletedSchedule_ReturnsEmpty() {
        // Given
        Schedule schedule = Schedule.builder()
                .title("삭제된 일정")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusHours(2))
                .scheduleDesc("삭제된 일정")
                .type(ScheduleType.OTHER)
                .account(testAccount)
                .team(testTeam)
                .build();
        schedule.markDeleted(); // 소프트 삭제
        Schedule savedSchedule = scheduleRepository.save(schedule);

        // When
        Optional<Schedule> result = scheduleRepository.findByIdWithDetailsAndIsDeletedFalse(savedSchedule.getId());

        // Then
        assertThat(result).isEmpty();
    }
}
