package com.pickteam.service.user;

public interface ValidationService {

    // 이메일 형식 유효성 검사
    boolean isValidEmail(String email);

    // 비밀번호 유효성 검사 (복잡성 검사)
    boolean isValidPassword(String password);

    // 이름 유효성 검사
    boolean isValidName(String name);

    // 나이 유효성 검사
    boolean isValidAge(Integer age);

    // MBTI 유효성 검사
    boolean isValidMbti(String mbti);

    // 비밀번호 강도 체크
    PasswordStrength getPasswordStrength(String password);

    // 유효성 검사 결과를 담는 enum
    enum PasswordStrength {
        WEAK, MEDIUM, STRONG, VERY_STRONG
    }
}
