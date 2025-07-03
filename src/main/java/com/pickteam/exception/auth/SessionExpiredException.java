package com.pickteam.exception.auth;

/**
 * 세션 만료 예외
 * - 다른 기기에서 로그인하여 현재 세션이 무효화된 경우 발생
 * - 중복 로그인 방지 정책에 의한 강제 로그아웃 시 사용
 */
public class SessionExpiredException extends RuntimeException {

    /**
     * 기본 생성자
     */
    public SessionExpiredException() {
        super("세션이 만료되었습니다. 다시 로그인해 주세요.");
    }

    /**
     * 메시지를 포함한 생성자
     * 
     * @param message 예외 메시지
     */
    public SessionExpiredException(String message) {
        super(message);
    }

    /**
     * 메시지와 원인을 포함한 생성자
     * 
     * @param message 예외 메시지
     * @param cause   원인이 되는 예외
     */
    public SessionExpiredException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 원인을 포함한 생성자
     * 
     * @param cause 원인이 되는 예외
     */
    public SessionExpiredException(Throwable cause) {
        super(cause);
    }
}
