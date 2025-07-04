package com.pickteam.dto.kanban;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KanbanTaskCreateRequest {
    @NotBlank
    private String subject;
    
    private String content;
    
    private LocalDateTime deadline;
    
    @NotNull
    private Long kanbanListId;
    
    private Integer order;
    
    private List<Long> assigneeIds;
} 