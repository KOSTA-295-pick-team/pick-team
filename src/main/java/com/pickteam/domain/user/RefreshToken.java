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
 * - 세션 관리 및 보안 추적을 위한 상세 정보 포함
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

    /** 로그인 시각 */
    @Column(nullable = false)
    private LocalDateTime loginTime;

    /** 마지막 사용 시각 */
    @Column(nullable = false)
    private LocalDateTime lastUsedTime;

    /** 클라이언트 IP 주소 */
    @Column(length = 45) // IPv6 주소도 지원
    private String ipAddress;

    /** 디바이스/클라이언트 정보 */
    @Column(length = 500)
    private String deviceInfo;

    /** 사용자 에이전트 정보 */
    @Column(length = 1000)
    private String userAgent;

    /** 세션 무효화 여부 */
    @Column(nullable = false)
    @Builder.Default
    private boolean invalidated = false;

    /** 세션 무효화 시각 */
    private LocalDateTime invalidatedAt;

    /**
     * 토큰 만료 여부 확인
     * 
     * @return 현재 시각이 만료 시각을 지났으면 true
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 세션이 유효한지 확인
     * (만료되지 않고 무효화되지 않았는지 확인)
     * 
     * @return 세션이 유효하면 true
     */
    public boolean isValid() {
        return !isExpired() && !invalidated;
    }

    /**
     * 세션 무효화
     */
    public void invalidate() {
        this.invalidated = true;
        this.invalidatedAt = LocalDateTime.now();
    }

    /**
     * 마지막 사용 시간 업데이트
     */
    public void updateLastUsedTime() {
        this.lastUsedTime = LocalDateTime.now();
    }

    /**
     * 디바이스 정보 문자열 생성
     * 
     * @return 디바이스 및 IP 정보를 포함한 문자열
     */
    public String getSessionInfo() {
        StringBuilder sb = new StringBuilder();
        if (deviceInfo != null && !deviceInfo.trim().isEmpty()) {
            sb.append(deviceInfo);
        }
        if (ipAddress != null && !ipAddress.trim().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" from ");
            }
            sb.append(ipAddress);
        }
        return sb.length() > 0 ? sb.toString() : "Unknown device";
    }
}
