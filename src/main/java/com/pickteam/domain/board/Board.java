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
    // 연관된 게시물 정보는 게시물이 hard-delete처리될 때, 같이 제거될 수 있도록 cascade 설정을 해준다.
    // 연관관계가 끊어진 객체를 자동으로 날려주도록 orphanRemoval을 붙여준다.
    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Post> postList = new ArrayList<>();

    // Soft-Delete 시, @SoftDelete 어노테이션은 자식객체에게 soft-delete를 전파해주지 않는다.
    // Soft-Delete 동작 전파를 위해 OnSoftDelete를 오버라이드 (검증 필요함)

    @Override
    public void onSoftDelete() {
        super.onSoftDelete();
        postList.forEach(BaseSoftDeleteByAnnotation::markDeleted);

    }

}