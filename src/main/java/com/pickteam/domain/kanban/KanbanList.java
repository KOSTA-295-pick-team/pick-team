package com.pickteam.domain.kanban;

import com.pickteam.domain.common.BaseSoftDeleteByAnnotation;
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
public class KanbanList extends BaseSoftDeleteByAnnotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String kanbanListName;

    @ManyToOne(optional = false)
    private Kanban kanban;

    @OneToMany(mappedBy = "kanbanList")
    private List<KanbanTask> tasks = new ArrayList<>();


}
