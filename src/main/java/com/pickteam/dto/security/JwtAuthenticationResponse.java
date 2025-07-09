package com.pickteam.dto.security;

import com.pickteam.dto.user.UserProfileResponse;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * JWT 로그인 성공 응답 DTO
 * - 로그인 성공 시 클라이언트에게 반환되는 토큰 정보
 * - Access Token과 Refresh Token을 포함
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthenticationResponse {
    /** JWT Access Token - API 요청 시 인증에 사용 */
    @com.fasterxml.jackson.annotation.JsonProperty("token")
    private String accessToken;

    /** JWT Refresh Token - Access Token 갱신 시 사용 */
    private String refreshToken;

    /** 토큰 타입 - 기본값 "Bearer" */
    @Builder.Default
    private String tokenType = "Bearer";

    /** Access Token 만료 시간 (밀리초) */
    private long expiresIn;

    /** 로그인한 사용자 정보 */
    private UserProfileResponse user;

    /**
     * OAuth 및 내부 로직용 토큰 접근자
     * JSON 직렬화에서는 제외되고, accessToken 필드가 "token"으로 직렬화됨
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getToken() {
        return this.accessToken;
    }

    /**
     * JWT 응답 생성자
     * 
     * @param accessToken  JWT Access Token
     * @param refreshToken JWT Refresh Token
     * @param expiresIn    토큰 만료 시간 (밀리초)
     * @param user         사용자 프로필 정보
     */
    public JwtAuthenticationResponse(String accessToken, String refreshToken, long expiresIn,
            UserProfileResponse user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }
}
