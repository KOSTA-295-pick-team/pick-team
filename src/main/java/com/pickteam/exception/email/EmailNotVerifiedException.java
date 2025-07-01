package com.pickteam.exception.email;

/**
 * 이메일 인증이 완료되지 않은 경우 발생하는 예외
 * - 회원가입 시 이메일 인증이 완료되지 않았을 때 사용
 * - 이메일 인증 필수 기능에서 검증 실패 시 사용
 */
public class EmailNotVerifiedException extends RuntimeException {

    /**
     * 기본 생성자
     */
    public EmailNotVerifiedException() {
        super("이메일 인증이 완료되지 않았습니다.");
    }

    /**
     * 메시지가 포함된 생성자
     * 
     * @param message 예외 메시지
     */
    public EmailNotVerifiedException(String message) {
        super(message);
    }

    /**
     * 메시지와 원인이 포함된 생성자
     * 
     * @param message 예외 메시지
     * @param cause   예외 원인
     */
    public EmailNotVerifiedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 원인이 포함된 생성자
     * 
     * @param cause 예외 원인
     */
    public EmailNotVerifiedException(Throwable cause) {
        super(cause);
    }
}
