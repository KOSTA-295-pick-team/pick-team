package com.pickteam.service.schedule;

import com.pickteam.domain.schedule.Schedule;
import com.pickteam.domain.schedule.ScheduleType;
import com.pickteam.domain.team.Team;
import com.pickteam.domain.user.Account;
import com.pickteam.dto.schedule.ScheduleCreateDto;
import com.pickteam.dto.schedule.ScheduleResponseDto;
import com.pickteam.dto.schedule.ScheduleUpdateDto;
import com.pickteam.repository.schedule.ScheduleRepository;
import com.pickteam.repository.team.TeamRepository;
import com.pickteam.repository.user.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * 일정 서비스 단위 테스트
 * @ExtendWith(MockitoExtension.class)를 사용하여 Mock 객체들을 주입
 * 비즈니스 로직만 단위 테스트로 검증
 */
@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @InjectMocks
    private ScheduleService scheduleService;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TeamRepository teamRepository;

    @Test
    @DisplayName("팀별 일정 목록을 조회할 수 있다")
    void getSchedules_ValidTeamId_ReturnsSchedules() {
        // Given
        Long teamId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        
        Schedule schedule = createTestSchedule();
        Page<Schedule> schedulePage = new PageImpl<>(List.of(schedule));

        given(scheduleRepository.findByTeamIdWithDetailsAndIsDeletedFalse(teamId, pageable))
                .willReturn(schedulePage);

        // When
        Page<ScheduleResponseDto> result = scheduleService.getSchedules(teamId, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("테스트 일정");
        verify(scheduleRepository).findByTeamIdWithDetailsAndIsDeletedFalse(teamId, pageable);
    }

    @Test
    @DisplayName("기간별 일정을 조회할 수 있다")
    void getSchedulesByDateRange_ValidDateRange_ReturnsSchedules() {
        // Given
        Long teamId = 1L;
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);
        
        Schedule schedule = createTestSchedule();

        given(scheduleRepository.findByTeamIdAndDateRangeAndIsDeletedFalse(teamId, startDate, endDate))
                .willReturn(List.of(schedule));

        // When
        List<ScheduleResponseDto> result = scheduleService.getSchedulesByDateRange(teamId, startDate, endDate);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("테스트 일정");
        verify(scheduleRepository).findByTeamIdAndDateRangeAndIsDeletedFalse(teamId, startDate, endDate);
    }

    @Test
    @DisplayName("일정 타입별로 일정을 조회할 수 있다")
    void getSchedulesByType_ValidType_ReturnsSchedules() {
        // Given
        Long teamId = 1L;
        ScheduleType type = ScheduleType.MEETING;
        Pageable pageable = PageRequest.of(0, 10);
        
        Schedule schedule = createTestSchedule();
        Page<Schedule> schedulePage = new PageImpl<>(List.of(schedule));

        given(scheduleRepository.findByTeamIdAndTypeAndIsDeletedFalse(teamId, type, pageable))
                .willReturn(schedulePage);

        // When
        Page<ScheduleResponseDto> result = scheduleService.getSchedulesByType(teamId, type, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("테스트 일정");
        verify(scheduleRepository).findByTeamIdAndTypeAndIsDeletedFalse(teamId, type, pageable);
    }

    @Test
    @DisplayName("내 일정을 조회할 수 있다")
    void getMySchedules_ValidAccountId_ReturnsSchedules() {
        // Given
        Long accountId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        
        Schedule schedule = createTestSchedule();
        Page<Schedule> schedulePage = new PageImpl<>(List.of(schedule));

        given(scheduleRepository.findByAccountIdAndIsDeletedFalse(accountId, pageable))
                .willReturn(schedulePage);

        // When
        Page<ScheduleResponseDto> result = scheduleService.getMySchedules(accountId, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("테스트 일정");
        verify(scheduleRepository).findByAccountIdAndIsDeletedFalse(accountId, pageable);
    }

    @Test
    @DisplayName("일정을 생성할 수 있다")
    void createSchedule_ValidRequest_ReturnsScheduleResponseDto() {
        // Given
        ScheduleCreateDto dto = createScheduleCreateDto();
        Long teamId = 1L;
        Long accountId = 1L;

        Account account = createTestAccount();
        Team team = createTestTeam();
        Schedule schedule = createTestSchedule();

        given(accountRepository.findById(accountId)).willReturn(Optional.of(account));
        given(teamRepository.findById(teamId)).willReturn(Optional.of(team));
        given(scheduleRepository.save(any(Schedule.class))).willReturn(schedule);

        // When
        ScheduleResponseDto result = scheduleService.createSchedule(dto, teamId, accountId);

        // Then
        assertThat(result.getTitle()).isEqualTo("테스트 일정");
        assertThat(result.getType()).isEqualTo(ScheduleType.MEETING);
        verify(accountRepository).findById(accountId);
        verify(teamRepository).findById(teamId);
        verify(scheduleRepository).save(any(Schedule.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 일정 생성 시 예외가 발생한다")
    void createSchedule_InvalidAccountId_ThrowsException() {
        // Given
        ScheduleCreateDto dto = createScheduleCreateDto();
        Long teamId = 1L;
        Long accountId = 999L;

        given(accountRepository.findById(accountId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> scheduleService.createSchedule(dto, teamId, accountId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("시작일이 종료일보다 늦을 때 예외가 발생한다")
    void createSchedule_InvalidDateRange_ThrowsException() {
        // Given
        ScheduleCreateDto dto = createScheduleCreateDto();
        dto.setStartDate(LocalDateTime.now().plusDays(2));
        dto.setEndDate(LocalDateTime.now().plusDays(1));
        Long teamId = 1L;
        Long accountId = 1L;

        Account account = createTestAccount();
        Team team = createTestTeam();

        given(accountRepository.findById(accountId)).willReturn(Optional.of(account));
        given(teamRepository.findById(teamId)).willReturn(Optional.of(team));

        // When & Then
        assertThatThrownBy(() -> scheduleService.createSchedule(dto, teamId, accountId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("시작 날짜는 종료 날짜보다 이전이어야 합니다");
    }

    @Test
    @DisplayName("일정을 수정할 수 있다")
    void updateSchedule_ValidRequest_ReturnsUpdatedScheduleResponseDto() {
        // Given
        Long scheduleId = 1L;
        Long accountId = 1L;
        ScheduleUpdateDto dto = createScheduleUpdateDto();

        Schedule schedule = createTestSchedule();
        schedule.getAccount().setId(accountId); // 소유자 설정

        given(scheduleRepository.findByIdWithDetailsAndIsDeletedFalse(scheduleId))
                .willReturn(Optional.of(schedule));

        // When
        ScheduleResponseDto result = scheduleService.updateSchedule(scheduleId, dto, accountId);

        // Then
        assertThat(result.getTitle()).isEqualTo("수정된 일정");
        assertThat(result.getType()).isEqualTo(ScheduleType.DEADLINE);
    }

    @Test
    @DisplayName("다른 사용자의 일정 수정 시 예외가 발생한다")
    void updateSchedule_DifferentOwner_ThrowsException() {
        // Given
        Long scheduleId = 1L;
        Long accountId = 999L; // 다른 사용자
        ScheduleUpdateDto dto = createScheduleUpdateDto();

        Schedule schedule = createTestSchedule();
        schedule.getAccount().setId(1L); // 원래 소유자

        given(scheduleRepository.findByIdWithDetailsAndIsDeletedFalse(scheduleId))
                .willReturn(Optional.of(schedule));

        // When & Then
        assertThatThrownBy(() -> scheduleService.updateSchedule(scheduleId, dto, accountId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("일정 작성자만 수정/삭제할 수 있습니다");
    }

    @Test
    @DisplayName("일정을 삭제할 수 있다")
    void deleteSchedule_ValidRequest_DeletesSchedule() {
        // Given
        Long scheduleId = 1L;
        Long accountId = 1L;

        Schedule schedule = createTestSchedule();
        schedule.getAccount().setId(accountId); // 소유자 설정

        given(scheduleRepository.findByIdAndIsDeletedFalse(scheduleId))
                .willReturn(Optional.of(schedule));

        // When
        scheduleService.deleteSchedule(scheduleId, accountId);

        // Then
        assertThat(schedule.getIsDeleted()).isTrue();
        assertThat(schedule.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("다른 사용자의 일정 삭제 시 예외가 발생한다")
    void deleteSchedule_DifferentOwner_ThrowsException() {
        // Given
        Long scheduleId = 1L;
        Long accountId = 999L; // 다른 사용자

        Schedule schedule = createTestSchedule();
        schedule.getAccount().setId(1L); // 원래 소유자

        given(scheduleRepository.findByIdAndIsDeletedFalse(scheduleId))
                .willReturn(Optional.of(schedule));

        // When & Then
        assertThatThrownBy(() -> scheduleService.deleteSchedule(scheduleId, accountId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("일정 작성자만 수정/삭제할 수 있습니다");
    }

    private Schedule createTestSchedule() {
        Account account = createTestAccount();
        Team team = createTestTeam();
        
        return Schedule.builder()
                .id(1L)
                .title("테스트 일정")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusHours(2))
                .scheduleDesc("테스트 일정 설명")
                .type(ScheduleType.MEETING)
                .account(account)
                .team(team)
                .build();
    }

    private Account createTestAccount() {
        return Account.builder()
                .id(1L)
                .email("test@example.com")
                .name("테스트 사용자")
                .build();
    }

    private Team createTestTeam() {
        return Team.builder()
                .id(1L)
                .name("테스트 팀")
                .build();
    }

    private ScheduleCreateDto createScheduleCreateDto() {
        ScheduleCreateDto dto = new ScheduleCreateDto();
        dto.setTitle("테스트 일정");
        dto.setStartDate(LocalDateTime.now());
        dto.setEndDate(LocalDateTime.now().plusHours(2));
        dto.setScheduleDesc("테스트 일정 설명");
        dto.setType(ScheduleType.MEETING);
        return dto;
    }

    private ScheduleUpdateDto createScheduleUpdateDto() {
        ScheduleUpdateDto dto = new ScheduleUpdateDto();
        dto.setTitle("수정된 일정");
        dto.setStartDate(LocalDateTime.now().plusDays(1));
        dto.setEndDate(LocalDateTime.now().plusDays(1).plusHours(2));
        dto.setScheduleDesc("수정된 일정 설명");
        dto.setType(ScheduleType.DEADLINE);
        return dto;
    }
}
