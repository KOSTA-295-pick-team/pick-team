package com.pickteam.dto.kanban;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class KanbanCreateRequest {
    
    @NotNull(message = "팀 ID는 필수입니다.")
    private Long teamId;
    
    @NotNull(message = "워크스페이스 ID는 필수입니다.")
    private Long workspaceId;
    
    private String name;
    private Integer order;
}