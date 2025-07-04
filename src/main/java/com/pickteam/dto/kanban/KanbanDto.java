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
public class KanbanDto {
    private Long id;
    private Long teamId;
    private Long workspaceId;
    private List<KanbanListDto> kanbanLists;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}