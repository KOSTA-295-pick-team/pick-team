package com.pickteam.domain.team;

import com.pickteam.domain.board.Board;
import com.pickteam.domain.common.BaseSoftDeleteByAnnotation;
import com.pickteam.domain.kanban.Kanban;
import com.pickteam.domain.workspace.Workspace;
import com.pickteam.domain.common.BaseTimeEntity;
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

    // Team이 Hard-Delete로 사라지면 해당 팀에 귀속된 다음 값들도 함께 사라져야 한다.
    @OneToMany(mappedBy = "team", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Board> boards = new ArrayList<>();
    @OneToMany(mappedBy = "team", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Kanban> kanbans = new ArrayList<>();
    @OneToMany(mappedBy = "team", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<TeamMember> teamMembers = new ArrayList<>();

}
