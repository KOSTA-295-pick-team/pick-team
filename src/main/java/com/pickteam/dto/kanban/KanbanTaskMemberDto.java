package com.pickteam.dto.kanban;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KanbanTaskMemberDto {
    private Long id;
    private Long kanbanTaskId;
    private Long accountId;
    private String memberName;
    private String profileImage;
    private LocalDateTime assignedAt;
} 