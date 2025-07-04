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
public class KanbanList extends BaseSoftDeleteSupportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String kanbanListName;
    
    @Column(name = "list_order")
    private Integer order;

    @ManyToOne(optional = false)
    private Kanban kanban;

    @OneToMany(mappedBy = "kanbanList")
    private List<KanbanTask> tasks = new ArrayList<>();


}
