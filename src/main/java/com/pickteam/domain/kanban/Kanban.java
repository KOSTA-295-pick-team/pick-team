package com.pickteam.domain.kanban;

import com.pickteam.domain.common.BaseSoftDeleteByAnnotation;
import com.pickteam.domain.team.Team;
import com.pickteam.domain.workspace.Workspace;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kanban extends BaseSoftDeleteByAnnotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Team team;

    @ManyToOne(optional = false)
    private Workspace workspace;
}
