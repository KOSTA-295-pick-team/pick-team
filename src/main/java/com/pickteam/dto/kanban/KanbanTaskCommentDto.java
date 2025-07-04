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
public class KanbanTaskCommentDto {
    private Long id;
    private String comment;
    private Long kanbanTaskId;
    private Long accountId;
    private String authorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 