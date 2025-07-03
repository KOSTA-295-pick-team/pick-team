package com.pickteam.exception.auth;

/**
 * 인증되지 않은 사용자 접근 예외
 * - JWT 토큰이 없거나 유효하지 않은 경우
 * - SecurityContext에서 인증 정보를 찾을 수 없는 경우
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
