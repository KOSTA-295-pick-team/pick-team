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
public class KanbanTask extends BaseSoftDeleteByAnnotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;

    @Lob
    private String content;

    private java.time.LocalDateTime deadline;

    @ManyToOne(optional = false)
    private KanbanList kanbanList;

    @OneToMany(mappedBy = "kanbanTask", orphanRemoval = true)
    private List<KanbanTaskAttach> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "kanbanTask", orphanRemoval = true)
    private List<KanbanTaskComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "kanbanTask", orphanRemoval = true)
    private List<KanbanTaskMember> members = new ArrayList<>();

    @Override
    public void markDeleted() {
        super.markDeleted();
        attachments.forEach(KanbanTaskAttach::markDeleted);
        comments.forEach(KanbanTaskComment::markDeleted);
        // members는 soft-delete 안 하기로 했으므로 제외
    }

}
