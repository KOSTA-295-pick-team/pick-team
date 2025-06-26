package com.pickteam.exception;

/**
 * 이메일 발송 실패 시 발생하는 예외
 * - SMTP 서버 연결 실패
 * - 잘못된 이메일 주소 형식
 * - 메일 서버 인증 실패 등의 경우에 사용
 */
public class EmailSendException extends RuntimeException {

    /**
     * 기본 생성자
     */
    public EmailSendException() {
        super("이메일 발송에 실패했습니다.");
    }

    /**
     * 메시지가 포함된 생성자
     * 
     * @param message 예외 메시지
     */
    public EmailSendException(String message) {
        super(message);
    }

    /**
     * 메시지와 원인이 포함된 생성자
     * 
     * @param message 예외 메시지
     * @param cause   예외 원인
     */
    public EmailSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
