package com.pickteam.exception.common;

import lombok.Getter;

/**
 * RFC 9457 Problem Detail 타입 열거형
 * - 도메인별로 구분된 에러 타입과 제목 정의
 * - 프론트엔드에서 타입 기반 처리를 위한 상수
 * - type(영문)과 title(영문)을 함께 관리
 */
@Getter
public enum ProblemType {

    // ===== 사용자 관련 에러 (USER_) =====
    USER_NOT_FOUND("USER_NOT_FOUND", "User Not Found"),
    DUPLICATE_EMAIL("DUPLICATE_EMAIL", "Duplicate Email"),
    ACCOUNT_WITHDRAWAL_IN_PROGRESS("ACCOUNT_WITHDRAWAL_IN_PROGRESS", "Account Withdrawal In Progress"),
    EMAIL_NOT_VERIFIED("EMAIL_NOT_VERIFIED", "Email Not Verified"),
    OAUTH_DELETED_ACCOUNT("OAUTH_DELETED_ACCOUNT", "OAuth Deleted Account"),

    // ===== 인증 관련 에러 (AUTH_) =====
    AUTHENTICATION_FAILED("AUTHENTICATION_FAILED", "Authentication Failed"),
    INVALID_TOKEN("INVALID_TOKEN", "Invalid Token"),
    SESSION_EXPIRED("SESSION_EXPIRED", "Session Expired"),
    UNAUTHORIZED_ACCESS("UNAUTHORIZED_ACCESS", "Unauthorized Access"),

    // ===== 이메일 관련 에러 (EMAIL_) =====
    EMAIL_SEND_FAILED("EMAIL_SEND_FAILED", "Email Send Failed"),
    EMAIL_VERIFICATION_FAILED("EMAIL_VERIFICATION_FAILED", "Email Verification Failed"),
    EMAIL_VERIFICATION_EXPIRED("EMAIL_VERIFICATION_EXPIRED", "Email Verification Expired"),

    // ===== 검증 관련 에러 (VALIDATION_) =====
    VALIDATION_FAILED("VALIDATION_FAILED", "Validation Failed"),
    BUSINESS_VALIDATION_FAILED("BUSINESS_VALIDATION_FAILED", "Business Validation Failed"),
    EMAIL_FORMAT_INVALID("EMAIL_FORMAT_INVALID", "Email Format Invalid"),
    PASSWORD_FORMAT_INVALID("PASSWORD_FORMAT_INVALID", "Password Format Invalid"),

    // ===== 시스템 관련 에러 (SYS_) =====
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "Internal Server Error"),
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", "Service Unavailable"),
    NETWORK_ERROR("NETWORK_ERROR", "Network Error"),
    UNEXPECTED_ERROR("UNEXPECTED_ERROR", "Unexpected Error"),
    ILLEGAL_STATE("ILLEGAL_STATE", "Illegal State"),

    // ===== 데이터 무결성 관련 에러 (DATA_) =====
    DATA_INTEGRITY_VIOLATION("DATA_INTEGRITY_VIOLATION", "Data Integrity Violation"),
    CONSTRAINT_VIOLATION("CONSTRAINT_VIOLATION", "Constraint Violation"),
    DUPLICATE_ENTRY("DUPLICATE_ENTRY", "Duplicate Entry"),

    // ===== 리소스 관련 에러 (RESOURCE_) =====
    NOT_FOUND("RESOURCE_NOT_FOUND", "Resource Not Found");

    private final String type;
    private final String title;

    ProblemType(String type, String title) {
        this.type = type;
        this.title = title;
    }
}
