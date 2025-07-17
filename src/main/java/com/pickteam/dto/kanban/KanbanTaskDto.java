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
public class KanbanTaskDto {
    private Long id;
    private String subject;
    private String content;
    private LocalDateTime deadline;
    private Long kanbanListId;
    private Integer order;
    private Boolean isApproved;
    private Boolean completionRequested;
    private String completionRequestMessage;
    private List<KanbanTaskAttachDto> attachments;
    private List<KanbanTaskCommentDto> comments;
    private List<KanbanTaskMemberDto> members;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 