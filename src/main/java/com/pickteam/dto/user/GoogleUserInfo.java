package com.pickteam.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pickteam.domain.enums.AuthProvider;
import lombok.Data;

/**
 * 구글 OAuth 사용자 정보 DTO
 * - 구글 OAuth API 응답을 매핑하는 DTO
 * - 구글 People API v1 또는 Google+ API 응답 형식
 */
@Data
public class GoogleUserInfo {

    /** 구글 사용자 고유 ID */
    @JsonProperty("id")
    private String id;

    /** 사용자 이메일 */
    @JsonProperty("email")
    private String email;

    /** 이메일 인증 여부 */
    @JsonProperty("verified_email")
    private boolean verifiedEmail;

    /** 사용자 이름 */
    @JsonProperty("name")
    private String name;

    /** 사용자 성 */
    @JsonProperty("given_name")
    private String givenName;

    /** 사용자 이름 */
    @JsonProperty("family_name")
    private String familyName;

    /** 프로필 이미지 URL */
    @JsonProperty("picture")
    private String picture;

    /** 구글 프로필 로케일 (언어 설정) */
    @JsonProperty("locale")
    private String locale;

    /**
     * 구글 사용자 정보를 공통 OAuthUserInfo로 변환
     * 
     * @return OAuthUserInfo 공통 DTO
     */
    public OAuthUserInfo toOAuthUserInfo() {
        return OAuthUserInfo.builder()
                .providerId(this.id)
                .email(this.email)
                .name(this.name)
                .profileImageUrl(this.picture)
                .provider(AuthProvider.GOOGLE)
                .emailVerified(this.verifiedEmail)
                .build();
    }

    /**
     * 필수 정보 유효성 검사
     * 
     * @return ID와 이메일이 모두 있으면 true
     */
    public boolean isValid() {
        return id != null && !id.trim().isEmpty() &&
                email != null && !email.trim().isEmpty();
    }
}
