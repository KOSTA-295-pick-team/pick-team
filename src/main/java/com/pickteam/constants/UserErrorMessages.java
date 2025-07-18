package com.pickteam.constants;

/**
 * 사용자 관련 에러 메시지 상수
 * - 일관된 에러 메시지 관리
 * - 다국어 지원을 위한 중앙화된 메시지 관리
 * - 유지보수성 향상을 위한 하드코딩 제거
 */
public class UserErrorMessages {

    // 유효성 검사 관련 메시지
    public static final String INVALID_EMAIL = "이메일 형식이 올바르지 않습니다.";
    public static final String INVALID_PASSWORD = "비밀번호 형식이 올바르지 않습니다.";
    public static final String INVALID_NEW_PASSWORD = "새 비밀번호 형식이 올바르지 않습니다.";
    public static final String INVALID_NAME = "이름 형식이 올바르지 않습니다.";
    public static final String INVALID_AGE_REGISTER = "나이는 0세 이상 225세 이하여야 합니다.";
    public static final String INVALID_AGE_UPDATE = "나이는 0세 이상 225세 이하여야 합니다.";
    public static final String INVALID_MBTI = "MBTI 형식이 올바르지 않습니다.";

    // 중복 및 존재성 관련 메시지
    public static final String DUPLICATE_EMAIL = "이미 사용 중인 이메일입니다.";
    public static final String USER_NOT_FOUND = "사용자를 찾을 수 없습니다.";

    // 인증 관련 메시지
    public static final String EMAIL_NOT_VERIFIED = "이메일 인증이 완료되지 않았습니다.";
    public static final String INVALID_CURRENT_PASSWORD = "현재 비밀번호가 올바르지 않습니다.";

    // 탈퇴 관련 메시지
    public static final String ACCOUNT_UNDER_WITHDRAWAL = "해당 이메일은 탈퇴 처리 중입니다. 완전 삭제 예정일: %s (남은 일수: %d일)";
    public static final String ACCOUNT_WITHDRAWAL_GRACE_PERIOD = "탈퇴한 계정의 유예 기간 중에는 동일한 이메일로 가입할 수 없습니다.";
    public static final String EMAIL_VERIFICATION_BLOCKED_WITHDRAWAL = "탈퇴 처리 중인 계정의 이메일로는 인증을 진행할 수 없습니다.";

    // Private 생성자로 인스턴스화 방지
    private UserErrorMessages() {
        throw new AssertionError("상수 클래스는 인스턴스화할 수 없습니다.");
    }
}
