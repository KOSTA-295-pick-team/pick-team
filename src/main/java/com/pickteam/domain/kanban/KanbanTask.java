package com.pickteam.domain.kanban;

import com.pickteam.domain.common.BaseSoftDeleteByAnnotation;
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

    @ManyToOne(optional = false)
    private KanbanList kanbanList;

    @OneToMany(mappedBy = "kanbanTask")
    private List<KanbanTaskAttach> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "kanbanTask")
    private List<KanbanTaskComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "kanbanTask")
    private List<KanbanTaskMember> members = new ArrayList<>();

}
