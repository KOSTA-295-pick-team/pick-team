package com.pickteam.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pickteam.domain.enums.AuthProvider;
import lombok.Data;

/**
 * 카카오 OAuth 사용자 정보 DTO
 * - 카카오 OAuth API 응답을 매핑하는 DTO
 * - 카카오 사용자 정보 API 응답 형식
 */
@Data
public class KakaoUserInfo {

    /** 카카오 사용자 고유 ID */
    @JsonProperty("id")
    private Long id;

    /** 카카오 계정 정보 */
    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    /** 카카오 프로필 정보 */
    @JsonProperty("properties")
    private KakaoProperties properties;

    /**
     * 카카오 계정 정보 내부 클래스
     */
    @Data
    public static class KakaoAccount {

        /** 이메일 주소 */
        @JsonProperty("email")
        private String email;

        /** 이메일 인증 여부 */
        @JsonProperty("is_email_verified")
        private boolean emailVerified;

        /** 이메일 유효성 */
        @JsonProperty("is_email_valid")
        private boolean emailValid;

        /** 프로필 정보 */
        @JsonProperty("profile")
        private KakaoProfile profile;
    }

    /**
     * 카카오 프로필 정보 내부 클래스
     */
    @Data
    public static class KakaoProfile {

        /** 닉네임 */
        @JsonProperty("nickname")
        private String nickname;

        /** 프로필 이미지 URL */
        @JsonProperty("profile_image_url")
        private String profileImageUrl;

        /** 썸네일 이미지 URL */
        @JsonProperty("thumbnail_image_url")
        private String thumbnailImageUrl;
    }

    /**
     * 카카오 기본 프로필 정보 (properties)
     */
    @Data
    public static class KakaoProperties {

        /** 닉네임 */
        @JsonProperty("nickname")
        private String nickname;

        /** 프로필 이미지 */
        @JsonProperty("profile_image")
        private String profileImage;

        /** 썸네일 이미지 */
        @JsonProperty("thumbnail_image")
        private String thumbnailImage;
    }

    /**
     * 카카오 사용자 정보를 공통 OAuthUserInfo로 변환
     * 
     * @return OAuthUserInfo 공통 DTO
     */
    public OAuthUserInfo toOAuthUserInfo() {
        String email = null;
        String name = null;
        String profileImageUrl = null;
        boolean emailVerified = false;

        // 이메일 정보 추출
        if (kakaoAccount != null) {
            email = kakaoAccount.getEmail();
            emailVerified = kakaoAccount.isEmailVerified();

            // 프로필 정보 추출 (kakao_account > profile)
            if (kakaoAccount.getProfile() != null) {
                name = kakaoAccount.getProfile().getNickname();
                profileImageUrl = kakaoAccount.getProfile().getProfileImageUrl();
            }
        }

        // properties에서 프로필 정보 추출 (백업)
        if (name == null && properties != null) {
            name = properties.getNickname();
        }
        if (profileImageUrl == null && properties != null) {
            profileImageUrl = properties.getProfileImage();
        }

        return OAuthUserInfo.builder()
                .providerId(String.valueOf(this.id))
                .email(email)
                .name(name)
                .profileImageUrl(profileImageUrl)
                .provider(AuthProvider.KAKAO)
                .emailVerified(emailVerified)
                .build();
    }

    /**
     * 필수 정보 유효성 검사
     * 
     * @return ID가 있으면 true (카카오는 이메일이 선택사항)
     */
    public boolean isValid() {
        return id != null;
    }
}
