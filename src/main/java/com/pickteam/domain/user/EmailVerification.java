package com.pickteam.domain.user;

import com.pickteam.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 이메일 인증 엔티티
 * - 회원가입 시 이메일 주소 소유 여부를 확인하기 위한 인증 정보
 * - 시간 제한이 있는 인증 코드를 관리
 * - 인증 완료 상태 추적
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerification extends BaseTimeEntity {

    /** 이메일 인증 고유 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 인증 대상 이메일 주소 */
    @Column(nullable = false)
    private String email;

    /** 이메일로 발송된 인증 코드 (숫자 또는 문자열) */
    @Column(nullable = false)
    private String verificationCode;

    /** 인증 코드 만료 시각 */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /** 인증 완료 여부 (기본값: false) */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    /**
     * 인증 코드 만료 여부 확인
     * 
     * @return 현재 시각이 만료 시각을 지났으면 true
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
