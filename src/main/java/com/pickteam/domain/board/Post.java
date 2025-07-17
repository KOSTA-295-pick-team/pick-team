package com.pickteam.domain.board;

import com.pickteam.domain.common.BaseSoftDeleteByAnnotation;
import com.pickteam.domain.common.BaseSoftDeleteSupportEntity;
import com.pickteam.domain.user.Account;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends BaseSoftDeleteSupportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer postNo;
    //DB의 기능을 사용하지 않고 Service단에서 board_id가 같은 마지막 포스트 번호를 쿼리해 수동 관리한다. (annotation 없음)

    private String title;

    /**
     * 게시글 작성자
     * - LAZY 로딩으로 성능 최적화 (수동 Soft Delete 방식에서 지원)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Account account;


    @Lob
    private String content;

    /**
     * 소속 게시판
     * - LAZY 로딩으로 성능 최적화
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    // 하나의 게시물에는 다수의 첨부파일, 댓글 등이 붙을 수 있다. 이를 관리하기 위해 OneToMany로 설정한다.
    // cascade 설정은 하지 않는다.
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    @Where(clause = "is_deleted = false") // 삭제되지 않은 첨부파일만 조회
    private List<PostAttach> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    @Where(clause = "is_deleted = false") // 삭제되지 않은 댓글만 조회
    private List<Comment> comments = new ArrayList<>();
}