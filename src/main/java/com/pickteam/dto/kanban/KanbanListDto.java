package com.pickteam.dto.kanban;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KanbanListDto {
    private Long id;
    private String kanbanListName;
    private Long kanbanId;
    private List<KanbanTaskDto> tasks;
    private Integer order;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 