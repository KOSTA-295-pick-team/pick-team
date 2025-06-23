package com.pickteam.domain.board;

import com.pickteam.domain.common.BaseSoftDeleteByAnnotation;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends BaseSoftDeleteByAnnotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer postNo;
    //DB의 기능을 사용하지 않고 Service단에서 board_id가 같은 마지막 포스트 번호를 쿼리해 수동 관리한다. (annotation 없음)

    private String title;

    private String author;

    @Lob
    private String content;

    @ManyToOne(optional = false)
    private Board board;

    // 하나의 게시물에는 다수의 첨부파일, 댓글 등이 붙을 수 있다. 이를 관리하기 위해 OneToMany로 설정한다.
    // 연관된 첨부파일 정보는 게시물이 hard-delete처리될 때, 같이 제거될 수 있도록 cascade 설정을 해준다.
    // 연관관계가 끊어진 객체를 자동으로 날려주도록 orphanRemoval을 붙여준다.
    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<PostAttach> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // Soft-Delete 시, @SoftDelete 어노테이션은 자식객체에게 soft-delete를 전파해주지 않는다.
    // Soft-Delete 동작 전파를 위해 OnSoftDelete를 오버라이드 (검증 필요함)

    @Override
    public void onSoftDelete() {
        super.onSoftDelete();
        comments.forEach(BaseSoftDeleteByAnnotation::markDeleted);
        attachments.forEach(BaseSoftDeleteByAnnotation::markDeleted);
    }

}