package com.pickteam.dto.schedule;

import com.pickteam.domain.schedule.ScheduleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ScheduleCreateDto {

    @NotBlank(message = "일정 제목은 필수입니다.")
    private String title;

    @NotNull(message = "시작 날짜는 필수입니다.")
    private LocalDateTime startDate;

    @NotNull(message = "종료 날짜는 필수입니다.")
    private LocalDateTime endDate;

    private String scheduleDesc;

    @NotNull(message = "일정 유형은 필수입니다.")
    private ScheduleType type;

    // teamId 제거 - PathVariable에서 받음
}