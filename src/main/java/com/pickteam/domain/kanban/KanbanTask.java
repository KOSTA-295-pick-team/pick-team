package com.pickteam.domain.kanban;

import com.pickteam.domain.common.BaseSoftDeleteSupportEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanbanTask extends BaseSoftDeleteSupportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;

    @Lob
    private String content;

    private java.time.LocalDateTime deadline;
    
    @Column(name = "task_order")
    private Integer order;
    
    @Column(name = "is_approved")
    @Builder.Default
    private Boolean isApproved = false;
    
    @Column(name = "completion_requested")
    @Builder.Default
    private Boolean completionRequested = false;
    
    @Column(name = "completion_request_message")
    private String completionRequestMessage;

    @ManyToOne(optional = false)
    private KanbanList kanbanList;

    @OneToMany(mappedBy = "kanbanTask")
    @Builder.Default
    private List<KanbanTaskAttach> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "kanbanTask")
    @Builder.Default
    private List<KanbanTaskComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "kanbanTask")
    @Builder.Default
    private List<KanbanTaskMember> members = new ArrayList<>();
}
