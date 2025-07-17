package com.pickteam.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * OAuth 인증 제공자 열거형
 * - 로컬 회원가입과 소셜 로그인 구분
 * - 각 제공자별 고유 식별자와 설명 제공
 */
@Getter
@RequiredArgsConstructor
public enum AuthProvider {

    /** 로컬 회원가입 (이메일 + 비밀번호) */
    LOCAL("로컬 회원가입"),

    /** 구글 OAuth 로그인 */
    GOOGLE("구글"),

    /** 카카오 OAuth 로그인 */
    KAKAO("카카오");

    /** 제공자 설명 */
    private final String description;

    /**
     * 문자열로부터 AuthProvider 찾기
     * 
     * @param provider 제공자 문자열 (대소문자 무관)
     * @return AuthProvider 열거형
     * @throws IllegalArgumentException null이거나 지원하지 않는 제공자인 경우
     */
    public static AuthProvider fromString(String provider) {
        if (provider == null) {
            throw new IllegalArgumentException("OAuth 제공자는 null일 수 없습니다");
        }

        try {
            return AuthProvider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("지원하지 않는 OAuth 제공자입니다: " + provider);
        }
    }

    /**
     * 소셜 로그인 제공자인지 확인
     * 
     * @return 소셜 로그인이면 true, 로컬 회원가입이면 false
     */
    public boolean isSocial() {
        return this != LOCAL;
    }
}
