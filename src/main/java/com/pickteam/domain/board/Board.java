package com.pickteam.domain.board;

import com.pickteam.domain.common.BaseSoftDeleteByAnnotation;
import com.pickteam.domain.common.BaseTimeEntity;
import com.pickteam.domain.team.Team;
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
public class Board extends BaseSoftDeleteByAnnotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Team team;

    // 하나의 게시판에는 다수의 게시물이 붙는다. 이를 관리하기 위해 OneToMany로 설정한다.
    // @SoftDelete annotation이 자식 엔티티에 대한 soft-delete를 불완전하게 지원한다. 따라서 cascade 설정은 하지 않는다.
    // 연관 엔티티에 대한 soft-delete는 필요할 경우 repository 레이어나 service 레이어에서 순서를 정해 호출하는 별도 메소드를 정의한다
    @OneToMany(mappedBy = "board")
    private List<Post> postList = new ArrayList<>();


}