package com.pickteam.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 세션 상태 응답 DTO
 * - 현재 사용자의 세션 유효성 및 로그인 정보 제공
 * - 프론트엔드에서 세션 상태를 주기적으로 확인하는 데 사용
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionStatusResponse {

    /** 세션 유효 여부 */
    private boolean isValid;

    /** 로그인 시간 */
    private LocalDateTime loginTime;

    /** 토큰 만료 시간 */
    private LocalDateTime expiresAt;

    /** 사용자 ID */
    private Long userId;

    /** 사용자 이메일 */
    private String email;
}
