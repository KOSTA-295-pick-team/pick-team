package com.pickteam.service.user;

import org.springframework.stereotype.Service;
import java.util.regex.Pattern;

/**
 * 유효성 검사 서비스 구현체
 * - 사용자 입력값에 대한 포맷 및 규칙 검증
 * - 정규표현식을 활용한 안전하고 정확한 유효성 검사
 * - 보안 강화를 위한 비밀번호 복잡성 검증
 */
@Service
public class ValidationServiceImpl implements ValidationService {

    /** 이메일 형식 검증을 위한 정규표현식 */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    /**
     * 비밀번호 복잡성 검증을 위한 정규표현식
     * - 최소 8자리 이상
     * - 대문자, 소문자, 숫자, 특수문자 각각 1개 이상 포함
     * - 공백 문자 불허
     */
    private static final Pattern PASSWORD_PATTERN = Pattern
            .compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");

    /** 이름 형식 검증을 위한 정규표현식 (한글, 영문, 공백 허용, 2-50자) */
    private static final Pattern NAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z\\s]{2,50}$");

    /** MBTI 16가지 유형 상수 배열 */
    private static final String[] VALID_MBTI = {
            "INTJ", "INTP", "ENTJ", "ENTP", "INFJ", "INFP", "ENFJ", "ENFP",
            "ISTJ", "ISFJ", "ESTJ", "ESFJ", "ISTP", "ISFP", "ESTP", "ESFP"
    };

    /**
     * 이메일 형식 유효성 검사
     * - 기본적인 이메일 패턴 검증 (@, 도메인 포함)
     * - null 값 체크 포함
     * 
     * @param email 검증할 이메일 주소
     * @return 이메일 형식 유효성 여부
     */
    @Override
    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 비밀번호 복잡성 유효성 검사
     * - 최소 8자리 이상
     * - 대문자, 소문자, 숫자, 특수문자(@#$%^&+=) 각각 1개 이상 포함
     * - 공백 문자 불허로 보안 강화
     * 
     * @param password 검증할 비밀번호
     * @return 비밀번호 복잡성 규칙 만족 여부
     */
    @Override
    public boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * 이름 형식 유효성 검사
     * - 한글, 영문 대소문자, 공백 허용
     * - 2자 이상 50자 이하 제한
     * - 숫자 및 특수문자 제외
     * 
     * @param name 검증할 이름
     * @return 이름 형식 유효성 여부
     */
    @Override
    public boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name).matches();
    }

    /**
     * 나이 유효성 검사
     * - 서비스 이용 가능 연령: 14세 이상 100세 이하
     * - null 값 체크 포함
     * 
     * @param age 검증할 나이
     * @return 나이 범위 유효성 여부
     */
    @Override
    public boolean isValidAge(Integer age) {
        return age != null && age >= 14 && age <= 100;
    }

    /**
     * MBTI 유효성 검사
     * - 16가지 표준 MBTI 유형과 비교
     * - 대소문자 구분 없이 검증 (대소문자 무시)
     * - Pick Team의 팀원 추천 알고리즘에 사용
     * 
     * @param mbti 검증할 MBTI 유형
     * @return MBTI 유형 유효성 여부
     */
    @Override
    public boolean isValidMbti(String mbti) {
        if (mbti == null)
            return false;

        for (String validMbti : VALID_MBTI) {
            if (validMbti.equalsIgnoreCase(mbti)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 비밀번호 강도 측정
     * - 길이, 문자 종류별 다양성을 기준으로 점수 계산
     * - 사용자에게 비밀번호 강도 피드백 제공
     * - 보안 강화를 위한 가이드라인 제시
     * 
     * @param password 강도를 측정할 비밀번호
     * @return 비밀번호 강도 (WEAK, MEDIUM, STRONG, VERY_STRONG)
     */
    @Override
    public PasswordStrength getPasswordStrength(String password) {
        if (password == null || password.length() < 6) {
            return PasswordStrength.WEAK;
        }

        int score = 0;

        // 길이 체크
        if (password.length() >= 8)
            score++;
        if (password.length() >= 12)
            score++;

        // 문자 종류 체크
        if (password.matches(".*[a-z].*"))
            score++;
        if (password.matches(".*[A-Z].*"))
            score++;
        if (password.matches(".*[0-9].*"))
            score++;
        if (password.matches(".*[@#$%^&+=!].*"))
            score++;

        return switch (score) {
            case 0, 1, 2 -> PasswordStrength.WEAK;
            case 3, 4 -> PasswordStrength.MEDIUM;
            case 5 -> PasswordStrength.STRONG;
            default -> PasswordStrength.VERY_STRONG;
        };
    }
}
