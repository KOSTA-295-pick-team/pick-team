package com.pickteam.constants;

/**
 * 세션 관련 에러 코드 상수
 * - 세션 만료, 중복 로그인 등 세션 관련 오류 코드 정의
 * - 일관된 에러 응답을 위한 표준화된 코드 제공
 * - 프론트엔드에서 에러 타입별 처리 가능
 */
public enum SessionErrorCode {
    
    // 세션 관련
    SESSION_EXPIRED("AUTH001", "세션이 만료되었습니다"),
    SESSION_INVALID("AUTH002", "유효하지 않은 세션입니다"),
    DUPLICATE_LOGIN("AUTH003", "다른 기기에서 로그인하여 세션이 만료되었습니다"),
    
    // 토큰 관련  
    TOKEN_EXPIRED("AUTH101", "토큰이 만료되었습니다"),
    TOKEN_INVALID("AUTH102", "유효하지 않은 토큰입니다"),
    REFRESH_TOKEN_EXPIRED("AUTH103", "리프레시 토큰이 만료되었습니다"),
    
    // 로그인 관련
    LOGIN_FAILED("AUTH201", "로그인에 실패했습니다"),
    ACCOUNT_LOCKED("AUTH202", "계정이 잠겨있습니다"),
    INVALID_CREDENTIALS("AUTH203", "이메일 또는 비밀번호가 올바르지 않습니다");
    
    private final String code;
    private final String message;
    
    SessionErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    /**
     * 에러 코드 반환
     * 
     * @return 에러 코드 (예: "AUTH001")
     */
    public String getCode() {
        return code;
    }
    
    /**
     * 에러 메시지 반환
     * 
     * @return 에러 메시지
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * 코드와 메시지를 포함한 문자열 반환
     * 
     * @return "코드: 메시지" 형태의 문자열
     */
    public String getFormattedMessage() {
        return String.format("[%s] %s", code, message);
    }
}
