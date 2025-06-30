package com.pickteam.exception.common;

/**
 * RFC 9457 Problem Detail 타입 상수
 * - 도메인별로 구분된 에러 타입 정의
 * - 프론트엔드에서 타입 기반 처리를 위한 상수
 */
public class ProblemType {

    // ===== 사용자 관련 에러 (USER_) =====
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String USER_EMAIL_DUPLICATE = "USER_EMAIL_DUPLICATE";
    public static final String USER_VALIDATION_FAILED = "USER_VALIDATION_FAILED";
    public static final String USER_WITHDRAWAL_IN_PROGRESS = "USER_WITHDRAWAL_IN_PROGRESS";
    public static final String USER_EMAIL_NOT_VERIFIED = "USER_EMAIL_NOT_VERIFIED";

    // ===== 인증 관련 에러 (AUTH_) =====
    public static final String AUTH_LOGIN_FAILED = "AUTH_LOGIN_FAILED";
    public static final String AUTH_TOKEN_INVALID = "AUTH_TOKEN_INVALID";
    public static final String AUTH_TOKEN_EXPIRED = "AUTH_TOKEN_EXPIRED";
    public static final String AUTH_UNAUTHORIZED = "AUTH_UNAUTHORIZED";
    public static final String AUTH_SESSION_EXPIRED = "AUTH_SESSION_EXPIRED";

    // ===== 이메일 관련 에러 (EMAIL_) =====
    public static final String EMAIL_SEND_FAILED = "EMAIL_SEND_FAILED";
    public static final String EMAIL_VERIFICATION_FAILED = "EMAIL_VERIFICATION_FAILED";
    public static final String EMAIL_VERIFICATION_EXPIRED = "EMAIL_VERIFICATION_EXPIRED";
    public static final String EMAIL_VERIFICATION_BLOCKED = "EMAIL_VERIFICATION_BLOCKED";

    // ===== 검증 관련 에러 (VALIDATION_) =====
    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String VALIDATION_EMAIL_FORMAT = "VALIDATION_EMAIL_FORMAT";
    public static final String VALIDATION_PASSWORD_FORMAT = "VALIDATION_PASSWORD_FORMAT";

    // ===== 시스템 관련 에러 (SYS_) =====
    public static final String SYS_DATABASE_ERROR = "SYS_DATABASE_ERROR";
    public static final String SYS_NETWORK_ERROR = "SYS_NETWORK_ERROR";
    public static final String SYS_INTERNAL_ERROR = "SYS_INTERNAL_ERROR";
    public static final String SYS_SERVICE_UNAVAILABLE = "SYS_SERVICE_UNAVAILABLE";

    // ===== 데이터 무결성 관련 에러 (DATA_) =====
    public static final String DATA_INTEGRITY_VIOLATION = "DATA_INTEGRITY_VIOLATION";
    public static final String DATA_CONSTRAINT_VIOLATION = "DATA_CONSTRAINT_VIOLATION";
    public static final String DATA_DUPLICATE_ENTRY = "DATA_DUPLICATE_ENTRY";

    // Private 생성자로 인스턴스화 방지
    private ProblemType() {
        throw new AssertionError("상수 클래스는 인스턴스화할 수 없습니다.");
    }
}
