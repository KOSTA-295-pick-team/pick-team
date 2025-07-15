package com.pickteam.dto.kanban;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class KanbanListUpdateRequest {

    private String kanbanListName;
    private Integer order;
} 