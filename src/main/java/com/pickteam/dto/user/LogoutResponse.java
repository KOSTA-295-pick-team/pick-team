package com.pickteam.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 로그아웃 응답 DTO
 * - 로그아웃 성공 시 반환되는 정보
 * - 로그아웃 시간 및 세션 정리 상태 제공
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutResponse {

    /** 로그아웃 시간 */
    private LocalDateTime logoutTime;

    /** 무효화된 세션 수 */
    private int invalidatedSessions;

    /** 로그아웃 성공 메시지 */
    private String message;
}
