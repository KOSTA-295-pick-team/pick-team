package com.pickteam.controller.schedule;

import com.pickteam.domain.schedule.ScheduleType;
import com.pickteam.dto.schedule.ScheduleCreateDto;
import com.pickteam.dto.schedule.ScheduleResponseDto;
import com.pickteam.dto.schedule.ScheduleUpdateDto;
import com.pickteam.service.schedule.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/teams/{teamId}/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    // 일정 생성
    @PostMapping
    public ResponseEntity<ScheduleResponseDto> createSchedule(
            @PathVariable Long teamId,
            @Valid @RequestBody ScheduleCreateDto dto,
            @RequestParam Long accountId) {

        // PathVariable의 teamId를 dto에 직접 설정
        ScheduleResponseDto schedule = scheduleService.createSchedule(dto, teamId, accountId);
        return ResponseEntity.ok(schedule);
    }

    // 일정 조회 (팀별)
    @GetMapping
    public ResponseEntity<Page<ScheduleResponseDto>> getSchedules(
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ScheduleType type) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").ascending());

        Page<ScheduleResponseDto> schedules;
        if (type != null) {
            schedules = scheduleService.getSchedulesByType(teamId, type, pageable);
        } else {
            schedules = scheduleService.getSchedules(teamId, pageable);
        }

        return ResponseEntity.ok(schedules);
    }

    // 기간별 일정 조회
    @GetMapping("/range")
    public ResponseEntity<List<ScheduleResponseDto>> getSchedulesByDateRange(
            @PathVariable Long teamId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<ScheduleResponseDto> schedules = scheduleService.getSchedulesByDateRange(teamId, startDate, endDate);
        return ResponseEntity.ok(schedules);
    }

    // 내 일정 조회
    @GetMapping("/my")
    public ResponseEntity<Page<ScheduleResponseDto>> getMySchedules(
            @PathVariable Long teamId,
            @RequestParam Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").ascending());
        Page<ScheduleResponseDto> schedules = scheduleService.getMySchedules(accountId, pageable);
        return ResponseEntity.ok(schedules);
    }

    // 일정 수정
    @PutMapping("/{scheduleId}")
    public ResponseEntity<ScheduleResponseDto> updateSchedule(
            @PathVariable Long teamId,
            @PathVariable Long scheduleId,
            @Valid @RequestBody ScheduleUpdateDto dto,
            @RequestParam Long accountId) {

        ScheduleResponseDto schedule = scheduleService.updateSchedule(scheduleId, dto, accountId);
        return ResponseEntity.ok(schedule);
    }

    // 일정 삭제
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable Long teamId,
            @PathVariable Long scheduleId,
            @RequestParam Long accountId) {

        scheduleService.deleteSchedule(scheduleId, accountId);
        return ResponseEntity.noContent().build();
    }
}