package com.pickteam.exception.auth;

/**
 * 토큰 검증 실패 시 발생하는 예외
 * - JWT 토큰이 유효하지 않거나 만료된 경우
 * - 리프레시 토큰이 일치하지 않거나 변조된 경우
 * - 토큰 형식이 올바르지 않은 경우
 */
public class InvalidTokenException extends RuntimeException {

    /**
     * 기본 생성자
     */
    public InvalidTokenException() {
        super();
    }

    /**
     * 메시지를 포함한 생성자
     * 
     * @param message 예외 메시지
     */
    public InvalidTokenException(String message) {
        super(message);
    }

    /**
     * 메시지와 원인을 포함한 생성자
     * 
     * @param message 예외 메시지
     * @param cause   예외 원인
     */
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 원인을 포함한 생성자
     * 
     * @param cause 예외 원인
     */
    public InvalidTokenException(Throwable cause) {
        super(cause);
    }
}
