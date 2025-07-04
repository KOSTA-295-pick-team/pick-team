package com.pickteam.dto.kanban;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KanbanTaskCommentCreateRequest {
    @NotBlank
    private String comment;
    
    @NotNull
    private Long kanbanTaskId;
} 