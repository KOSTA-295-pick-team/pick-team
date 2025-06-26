package com.pickteam.constants;

/**
 * 인증 관련 에러 메시지 상수
 * - 일관된 에러 메시지 관리
 * - JWT 토큰, 인증, 리프레시 토큰 관련 메시지
 * - 유지보수성 향상을 위한 하드코딩 제거
 */
public class AuthErrorMessages {

    // 인증 관련 메시지
    public static final String INVALID_CREDENTIALS = "이메일 또는 비밀번호가 올바르지 않습니다.";

    // 토큰 관련 메시지
    public static final String INVALID_REFRESH_TOKEN = "유효하지 않은 리프레시 토큰입니다.";
    public static final String EXPIRED_REFRESH_TOKEN = "만료된 리프레시 토큰입니다.";

    // 사용자 관련 메시지
    public static final String USER_NOT_FOUND = "사용자를 찾을 수 없습니다.";

    // Private 생성자로 인스턴스화 방지
    private AuthErrorMessages() {
        throw new AssertionError("상수 클래스는 인스턴스화할 수 없습니다.");
    }
}
