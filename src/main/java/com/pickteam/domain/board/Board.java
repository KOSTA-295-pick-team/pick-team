package com.pickteam.domain.board;

import com.pickteam.domain.common.BaseSoftDeleteByAnnotation;
import com.pickteam.domain.common.BaseSoftDeleteSupportEntity;
import com.pickteam.domain.common.BaseTimeEntity;
import com.pickteam.domain.team.Team;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 게시판 엔티티
 * - 팀별로 생성되는 게시판 정보를 관리
 * - 수동 Soft Delete 방식으로 삭제된 게시판도 데이터 보존
 * - LAZY 로딩 지원으로 성능 최적화
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Board extends BaseSoftDeleteSupportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 소속 팀 정보
     * - LAZY 로딩으로 성능 최적화 (수동 Soft Delete 방식에서 지원)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id")
    private Team team;

    // 하나의 게시판에는 다수의 게시물이 붙는다. 이를 관리하기 위해 OneToMany로 설정한다.
    // @SoftDelete annotation이 자식 엔티티에 대한 soft-delete를 불완전하게 지원한다. 따라서 cascade 설정은 하지 않는다.
    // 연관 엔티티에 대한 soft-delete는 필요할 경우 repository 레이어나 service 레이어에서 순서를 정해 호출하는 별도 메소드를 정의한다
    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY)
    private List<Post> postList = new ArrayList<>();


}