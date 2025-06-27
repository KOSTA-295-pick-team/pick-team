package com.pickteam.exception;

/**
 * 이메일 중복 시 발생하는 예외
 * - 회원가입 시 이미 존재하는 이메일로 가입 시도
 * - 이메일 변경 시 다른 사용자의 이메일과 중복
 * - 고유성 제약조건 위반 시 발생
 */
public class DuplicateEmailException extends RuntimeException {

    /**
     * 기본 생성자
     */
    public DuplicateEmailException() {
        super();
    }

    /**
     * 메시지를 포함한 생성자
     * 
     * @param message 예외 메시지
     */
    public DuplicateEmailException(String message) {
        super(message);
    }

    /**
     * 메시지와 원인을 포함한 생성자
     * 
     * @param message 예외 메시지
     * @param cause   예외 원인
     */
    public DuplicateEmailException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 원인을 포함한 생성자
     * 
     * @param cause 예외 원인
     */
    public DuplicateEmailException(Throwable cause) {
        super(cause);
    }
}
