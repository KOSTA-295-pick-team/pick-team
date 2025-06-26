package com.pickteam.service.user;

/**
 * 유효성 검사 서비스 인터페이스
 * - 사용자 입력값에 대한 포맷 및 규칙 검증
 * - 보안 강화를 위한 비밀번호 복잡성 검증
 * - Pick Team 서비스의 데이터 무결성 보장
 */
public interface ValidationService {

    /**
     * 이메일 주소의 형식을 검증합니다.
     * 기본적인 이메일 형식 (@, 도메인 포함) 검증을 수행합니다.
     * 
     * @param email 검증할 이메일 주소
     * @return 유효한 이메일 형식이면 true, 그렇지 않으면 false
     */
    boolean isValidEmail(String email);

    /**
     * 비밀번호의 복잡성을 검증합니다.
     * 최소 8자, 대소문자, 숫자, 특수문자(@#$%^&+=) 각각 1개 이상 포함 여부를 확인합니다.
     * 공백 문자는 보안상 허용하지 않습니다.
     * 
     * @param password 검증할 비밀번호
     * @return 복잡성 요구사항을 만족하면 true, 그렇지 않으면 false
     */
    boolean isValidPassword(String password);

    /**
     * 사용자 이름의 형식을 검증합니다.
     * 한글, 영문 대소문자, 공백만 허용하며 2자 이상 50자 이하로 제한합니다.
     * 숫자 및 특수문자는 허용하지 않습니다.
     * 
     * @param name 검증할 이름
     * @return 유효한 이름 형식이면 true, 그렇지 않으면 false
     */
    boolean isValidName(String name);

    /**
     * 사용자 나이의 유효성을 검증합니다.
     * 0세 이상 150세 이하의 범위에서 유효성을 확인합니다.
     * 
     * @param age 검증할 나이
     * @return 유효한 나이 범위이면 true, 그렇지 않으면 false
     */
    boolean isValidAge(Integer age);

    /**
     * MBTI 성격 유형의 유효성을 검증합니다.
     * 16가지 표준 MBTI 유형(INTJ, INTP, ENTJ, ENTP, INFJ, INFP, ENFJ, ENFP,
     * ISTJ, ISFJ, ESTJ, ESFJ, ISTP, ISFP, ESTP, ESFP)과 일치하는지 확인합니다.
     * 대소문자 구분 없이 검증됩니다.
     * 
     * @param mbti 검증할 MBTI 유형
     * @return 유효한 MBTI 유형이면 true, 그렇지 않으면 false
     */
    boolean isValidMbti(String mbti);

    /**
     * 비밀번호의 강도를 측정합니다.
     * 길이, 문자 종류별 다양성(대소문자, 숫자, 특수문자)을 기준으로
     * 점수를 계산하여 강도를 판정합니다.
     * 
     * @param password 강도를 측정할 비밀번호
     * @return 비밀번호 강도 (WEAK, MEDIUM, STRONG, VERY_STRONG)
     */
    PasswordStrength getPasswordStrength(String password);

    /**
     * 비밀번호 강도를 나타내는 열거형
     * - WEAK: 매우 약함 (6자 미만 또는 단순한 구성)
     * - MEDIUM: 보통 (8자 이상, 일부 문자 종류 포함)
     * - STRONG: 강함 (12자 이상, 다양한 문자 종류 포함)
     * - VERY_STRONG: 매우 강함 (모든 복잡성 요구사항 만족)
     */
    enum PasswordStrength {
        WEAK, MEDIUM, STRONG, VERY_STRONG
    }
}
