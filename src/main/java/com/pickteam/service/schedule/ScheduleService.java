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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final AccountRepository accountRepository;
    private final TeamRepository teamRepository;

    public Page<ScheduleResponseDto> getSchedules(Long teamId, Pageable pageable) {
        Page<Schedule> schedules = scheduleRepository.findByTeamIdWithDetails(teamId, pageable);
        return schedules.map(ScheduleResponseDto::from);
    }

    public List<ScheduleResponseDto> getSchedulesByDateRange(
            Long teamId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Schedule> schedules = scheduleRepository.findByTeamIdAndDateRange(teamId, startDate, endDate);
        return schedules.stream()
                .map(ScheduleResponseDto::from)
                .collect(Collectors.toList());
    }

    public Page<ScheduleResponseDto> getSchedulesByType(
            Long teamId, ScheduleType type, Pageable pageable) {
        Page<Schedule> schedules = scheduleRepository.findByTeamIdAndType(teamId, type, pageable);
        return schedules.map(ScheduleResponseDto::from);
    }

    public Page<ScheduleResponseDto> getMySchedules(Long accountId, Pageable pageable) {
        Page<Schedule> schedules = scheduleRepository.findByAccountId(accountId, pageable);
        return schedules.map(ScheduleResponseDto::from);
    }

    @Transactional
    public ScheduleResponseDto createSchedule(ScheduleCreateDto dto, Long teamId, Long accountId) {
        log.info("일정 생성 요청 - accountId: {}, teamId: {}, title: {}",
                accountId, teamId, dto.getTitle());

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. accountId: " + accountId));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다. teamId: " + teamId));

        validateScheduleDates(dto.getStartDate(), dto.getEndDate());

        Schedule schedule = Schedule.builder()
                .title(dto.getTitle())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .scheduleDesc(dto.getScheduleDesc())
                .type(dto.getType())
                .account(account)
                .team(team)
                .build();

        Schedule savedSchedule = scheduleRepository.save(schedule);
        log.info("일정 생성 완료 - scheduleId: {}", savedSchedule.getId());

        return ScheduleResponseDto.from(savedSchedule);
    }

    @Transactional
    public ScheduleResponseDto updateSchedule(Long scheduleId, ScheduleUpdateDto dto, Long accountId) {
        log.info("일정 수정 요청 - scheduleId: {}, accountId: {}", scheduleId, accountId);

        Schedule schedule = scheduleRepository.findByIdWithDetails(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다. scheduleId: " + scheduleId));

        validateScheduleOwner(schedule, accountId);
        validateScheduleDates(dto.getStartDate(), dto.getEndDate());

        schedule.setTitle(dto.getTitle());
        schedule.setStartDate(dto.getStartDate());
        schedule.setEndDate(dto.getEndDate());
        schedule.setScheduleDesc(dto.getScheduleDesc());
        schedule.setType(dto.getType());

        log.info("일정 수정 완료 - scheduleId: {}", scheduleId);
        return ScheduleResponseDto.from(schedule);
    }

    @Transactional
    public void deleteSchedule(Long scheduleId, Long accountId) {
        log.info("일정 삭제 요청 - scheduleId: {}, accountId: {}", scheduleId, accountId);

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다. scheduleId: " + scheduleId));

        validateScheduleOwner(schedule, accountId);

        scheduleRepository.delete(schedule);
        log.info("일정 삭제 완료 - scheduleId: {}", scheduleId);
    }

    private void validateScheduleOwner(Schedule schedule, Long accountId) {
        if (!schedule.getAccount().getId().equals(accountId)) {
            throw new IllegalArgumentException("일정 작성자만 수정/삭제할 수 있습니다.");
        }
    }

    private void validateScheduleDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 이전이어야 합니다.");
        }
    }
}