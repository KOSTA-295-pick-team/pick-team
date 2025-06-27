package com.pickteam.domain.user;

import com.pickteam.domain.common.BaseSoftDeleteByAnnotation;
import com.pickteam.domain.team.TeamMember;
import com.pickteam.domain.workspace.WorkspaceMember;
import jakarta.persistence.*;
import lombok.*;

/**
 * 사용자-해시태그 연결 엔티티 (중간 테이블)
 * - Account와 UserHashtag 간의 다대다 관계를 해결
 * - 사용자별 할당된 해시태그 목록 관리
 * - 팀 매칭 알고리즘에서 사용자 특성 분석에 활용
 * - Soft Delete 지원으로 해시태그 히스토리 추적 가능
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserHashtagList extends BaseSoftDeleteByAnnotation {

    /** 사용자-해시태그 연결 고유 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 해시태그를 보유한 사용자 */
    @ManyToOne(optional = false)
    private Account account;

    /** 사용자에게 할당된 해시태그 */
    @ManyToOne(optional = false)
    private UserHashtag userHashtag;

}
