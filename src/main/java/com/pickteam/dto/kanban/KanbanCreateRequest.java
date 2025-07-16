package com.pickteam.dto.kanban;

import lombok.Data;

@Data
public class KanbanCreateRequest {
    private Long teamId;
    private Long workspaceId;
    private String name;
    private Integer order;
}