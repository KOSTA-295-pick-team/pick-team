package com.pickteam.exception.validation;

/**
 * 유효성 검사 실패 시 발생하는 예외
 * - 이메일, 비밀번호, 이름, 나이, MBTI 등 입력값 형식 오류
 * - 비즈니스 규칙 위반 시 발생
 * - 사용자 입력 데이터의 무결성 검증 실패
 */
public class ValidationException extends RuntimeException {

    /**
     * 기본 생성자
     */
    public ValidationException() {
        super();
    }

    /**
     * 메시지를 포함한 생성자
     * 
     * @param message 예외 메시지
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * 메시지와 원인을 포함한 생성자
     * 
     * @param message 예외 메시지
     * @param cause   예외 원인
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 원인을 포함한 생성자
     * 
     * @param cause 예외 원인
     */
    public ValidationException(Throwable cause) {
        super(cause);
    }
}
