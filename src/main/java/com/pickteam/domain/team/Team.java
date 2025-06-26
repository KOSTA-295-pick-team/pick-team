package com.pickteam.domain.team;

import com.pickteam.domain.board.Board;
import com.pickteam.domain.common.BaseSoftDeleteByAnnotation;
import com.pickteam.domain.kanban.Kanban;
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
public class Team extends BaseSoftDeleteByAnnotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(optional = false)
    private Workspace workspace;

    @OneToMany(mappedBy = "team")
    private List<Board> boards = new ArrayList<>();
    @OneToMany(mappedBy = "team")
    private List<Kanban> kanbans = new ArrayList<>();
    @OneToMany(mappedBy = "team")
    private List<TeamMember> teamMembers = new ArrayList<>();


}
