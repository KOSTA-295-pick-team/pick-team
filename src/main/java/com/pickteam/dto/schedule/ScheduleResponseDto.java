package com.pickteam.dto.schedule;

import com.pickteam.domain.schedule.Schedule;
import com.pickteam.domain.schedule.ScheduleType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ScheduleResponseDto {
    
    private Long id;
    private String title;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String scheduleDesc;
    private ScheduleType type;
    private String typeName;
    private String creatorName;
    private Long creatorId;
    private String teamName;
    private Long teamId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ScheduleResponseDto from(Schedule schedule) {
        ScheduleResponseDto dto = new ScheduleResponseDto();
        dto.setId(schedule.getId());
        dto.setTitle(schedule.getTitle());
        dto.setStartDate(schedule.getStartDate());
        dto.setEndDate(schedule.getEndDate());
        dto.setScheduleDesc(schedule.getScheduleDesc());
        dto.setType(schedule.getType());
        dto.setTypeName(schedule.getType().getDescription());
        dto.setCreatorName(schedule.getAccount().getName());
        dto.setCreatorId(schedule.getAccount().getId());
        dto.setTeamName(schedule.getTeam().getName());
        dto.setTeamId(schedule.getTeam().getId());
        dto.setCreatedAt(schedule.getCreatedAt());
        dto.setUpdatedAt(schedule.getUpdatedAt());
        return dto;
    }
}