package com.pickteam.exception;

/**
 * 사용자를 찾을 수 없는 경우 발생하는 예외
 * - 로그인, 토큰 갱신 시 사용자 조회 실패
 * - 삭제된 계정이나 존재하지 않는 사용자 접근 시 사용
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * 기본 생성자
     */
    public UserNotFoundException() {
        super("사용자를 찾을 수 없습니다.");
    }

    /**
     * 메시지가 포함된 생성자
     * 
     * @param message 예외 메시지
     */
    public UserNotFoundException(String message) {
        super(message);
    }

    /**
     * 메시지와 원인이 포함된 생성자
     * 
     * @param message 예외 메시지
     * @param cause   예외 원인
     */
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
