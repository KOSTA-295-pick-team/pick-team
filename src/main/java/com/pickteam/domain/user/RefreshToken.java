package com.pickteam.domain.user;

import com.pickteam.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * JWT Refresh Token 엔티티
 * - JWT Access Token 갱신을 위한 Refresh Token 정보 관리
 * - 사용자별 토큰 저장 및 만료 시간 관리
 * - 보안을 위한 토큰 무효화 지원
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken extends BaseTimeEntity {

    /** Refresh Token 고유 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 토큰 소유자 (지연 로딩으로 성능 최적화) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /** JWT Refresh Token 값 (유니크 제약으로 중복 방지) */
    @Column(nullable = false, unique = true)
    private String token;

    /** 토큰 만료 시각 */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * 토큰 만료 여부 확인
     * 
     * @return 현재 시각이 만료 시각을 지났으면 true
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
