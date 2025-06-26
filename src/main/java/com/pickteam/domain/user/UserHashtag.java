package com.pickteam.domain.user;

import com.pickteam.domain.common.BaseSoftDeleteSupportEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 사용자 해시태그 엔티티
 * - 팀 매칭 알고리즘에 사용되는 해시태그 정보
 * - 사용자의 기술 스택, 관심사, 특성 등을 태그로 표현
 * - Soft Delete 지원으로 데이터 히스토리 보존
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserHashtag extends BaseSoftDeleteSupportEntity {

    /** 해시태그 고유 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 해시태그 이름 (예: "Java", "React", "팀워크", "리더십" 등) */
    private String name;
}
