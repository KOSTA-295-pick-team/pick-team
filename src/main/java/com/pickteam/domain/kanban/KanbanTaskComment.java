package com.pickteam.domain.kanban;

import com.pickteam.domain.common.BaseSoftDeleteByAnnotation;
import com.pickteam.domain.user.Account;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanbanTaskComment extends BaseSoftDeleteByAnnotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private String comment;

    @ManyToOne(optional = false)
    private KanbanTask kanbanTask;

    @ManyToOne(optional = false)
    private Account account;
}
