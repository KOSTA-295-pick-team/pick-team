package com.pickteam.constants;

/**
 * 이메일 관련 에러 메시지 상수
 * - 일관된 에러 메시지 관리
 * - 이메일 발송, 인증 관련 메시지
 * - 유지보수성 향상을 위한 하드코딩 제거
 */
public class EmailErrorMessages {

    // 이메일 발송 관련 메시지
    public static final String EMAIL_SEND_FAILED = "이메일 발송에 실패했습니다.";

    // 인증 관련 메시지
    public static final String VERIFICATION_CODE_EXPIRED = "만료된 인증 코드 사용 시도";
    public static final String EMAIL_VERIFICATION_SUCCESS = "이메일 인증 성공";

    // 이메일 내용 관련 상수
    public static final String EMAIL_SUBJECT = "Pick Team 이메일 인증";
    public static final String EMAIL_HEADER = "Pick Team 이메일 인증";
    public static final String EMAIL_GREETING = "안녕하세요! Pick Team 서비스 이용을 위해 이메일 인증을 완료해주세요.";
    public static final String EMAIL_CODE_INSTRUCTION = "위 인증 코드를 입력하여 이메일 인증을 완료해주세요.";
    public static final String EMAIL_EXPIRY_INFO = "인증 코드는 5분간 유효합니다.";

    // Private 생성자로 인스턴스화 방지
    private EmailErrorMessages() {
        throw new AssertionError("상수 클래스는 인스턴스화할 수 없습니다.");
    }
}
