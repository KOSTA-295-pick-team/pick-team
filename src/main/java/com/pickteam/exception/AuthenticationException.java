package com.pickteam.exception;

/**
 * 인증 실패 시 발생하는 예외
 * - 이메일 또는 비밀번호가 올바르지 않은 경우
 * - 계정이 비활성화되었거나 삭제된 경우
 * - 로그인 시도가 실패한 경우
 */
public class AuthenticationException extends RuntimeException {

    /**
     * 기본 생성자
     */
    public AuthenticationException() {
        super();
    }

    /**
     * 메시지를 포함한 생성자
     * 
     * @param message 예외 메시지
     */
    public AuthenticationException(String message) {
        super(message);
    }

    /**
     * 메시지와 원인을 포함한 생성자
     * 
     * @param message 예외 메시지
     * @param cause   예외 원인
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 원인을 포함한 생성자
     * 
     * @param cause 예외 원인
     */
    public AuthenticationException(Throwable cause) {
        super(cause);
    }
}
