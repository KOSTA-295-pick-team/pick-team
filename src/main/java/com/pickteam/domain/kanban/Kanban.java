package com.pickteam.domain.kanban;

import com.pickteam.domain.common.BaseSoftDeleteSupportEntity;
import com.pickteam.domain.team.Team;
import com.pickteam.domain.workspace.Workspace;
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
public class Kanban extends BaseSoftDeleteSupportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "kanban_order")
    private Integer order;

    @ManyToOne(optional = false)
    private Team team;

    @ManyToOne(optional = false)
    private Workspace workspace;

    @OneToMany(mappedBy = "kanban")
    private List<KanbanList> kanbanLists = new ArrayList<>();
}