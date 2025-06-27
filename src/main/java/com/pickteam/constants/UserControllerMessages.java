package com.pickteam.constants;

/**
 * UserController 응답 메시지 상수
 * - 일관된 응답 메시지 관리
 * - 사용자 관련 API 응답 메시지
 * - 유지보수성 향상을 위한 하드코딩 제거
 */
public class UserControllerMessages {

    // 회원가입 관련 메시지
    public static final String REGISTER_SUCCESS = "회원가입이 완료되었습니다.";

    // ID 중복검사 관련 메시지
    public static final String CHECK_DUPLICATE_SUCCESS = "중복 검사가 완료되었습니다.";

    // 비밀번호 검증 관련 메시지
    public static final String PASSWORD_VALIDATION_SUCCESS = "비밀번호 유효성 검사가 완료되었습니다.";

    // 이메일 인증 관련 메시지
    public static final String EMAIL_VERIFICATION_SENT = "인증 메일이 발송되었습니다.";
    public static final String EMAIL_VERIFICATION_SUCCESS = "이메일 인증이 확인되었습니다.";

    // 프로필 조회 관련 메시지
    public static final String PROFILE_GET_SUCCESS = "프로필 조회가 완료되었습니다.";
    public static final String USER_PROFILE_GET_SUCCESS = "사용자 프로필 조회가 완료되었습니다.";
    public static final String ALL_USER_PROFILE_GET_SUCCESS = "전체 사용자 프로필 조회가 완료되었습니다.";
    public static final String RECOMMENDED_MEMBERS_GET_SUCCESS = "추천 팀원 조회가 완료되었습니다.";

    // 로그인/로그아웃 관련 메시지
    public static final String LOGIN_SUCCESS = "로그인 성공";
    public static final String LOGOUT_SUCCESS = "로그아웃되었습니다.";

    // 프로필 관련 메시지
    public static final String PROFILE_UPDATE_SUCCESS = "프로필이 수정되었습니다.";

    // 비밀번호 관련 메시지
    public static final String PASSWORD_CHANGE_SUCCESS = "비밀번호가 변경되었습니다.";

    // 계정 관련 메시지
    public static final String ACCOUNT_DELETE_SUCCESS = "계정이 삭제되었습니다.";

    // Private 생성자로 인스턴스화 방지
    private UserControllerMessages() {
        throw new AssertionError("상수 클래스는 인스턴스화할 수 없습니다.");
    }
}
