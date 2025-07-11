package com.pickteam.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * OAuth 설정 구성 클래스
 * - application.properties의 OAuth 관련 설정값들을 매핑
 * - 구글, 카카오 OAuth 클라이언트 설정 관리
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.oauth")
public class OAuthConfig {

    /** 구글 OAuth 설정 */
    private Google google = new Google();

    /** 카카오 OAuth 설정 */
    private Kakao kakao = new Kakao();

    /**
     * 구글 OAuth 설정 내부 클래스
     */
    @Getter
    @Setter
    public static class Google {
        /** 구글 OAuth 클라이언트 ID */
        private String clientId;

        /** 구글 OAuth 클라이언트 시크릿 */
        private String clientSecret;

        /** 구글 OAuth 리다이렉트 URI */
        private String redirectUri;

        /**
         * 구글 OAuth 설정 유효성 검사
         * 
         * @return 모든 필수 설정이 있으면 true
         */
        public boolean isValid() {
            return clientId != null && !clientId.trim().isEmpty() &&
                    clientSecret != null && !clientSecret.trim().isEmpty() &&
                    redirectUri != null && !redirectUri.trim().isEmpty();
        }
    }

    /**
     * 카카오 OAuth 설정 내부 클래스
     */
    @Getter
    @Setter
    public static class Kakao {
        /** 카카오 OAuth 클라이언트 ID */
        private String clientId;

        /** 카카오 OAuth 클라이언트 시크릿 */
        private String clientSecret;

        /** 카카오 OAuth 리다이렉트 URI */
        private String redirectUri;

        /**
         * 카카오 OAuth 설정 유효성 검사
         * 
         * @return 모든 필수 설정이 있으면 true
         */
        public boolean isValid() {
            return clientId != null && !clientId.trim().isEmpty() &&
                    clientSecret != null && !clientSecret.trim().isEmpty() &&
                    redirectUri != null && !redirectUri.trim().isEmpty();
        }
    }

    /**
     * 전체 OAuth 설정 유효성 검사
     * 
     * @return 모든 OAuth 제공자 설정이 유효하면 true
     */
    public boolean isAllValid() {
        return google.isValid() && kakao.isValid();
    }

    /**
     * 특정 제공자 설정 유효성 검사
     * 
     * @param provider 제공자 이름 (google, kakao)
     * @return 해당 제공자 설정이 유효하면 true
     */
    public boolean isProviderValid(String provider) {
        if (provider == null) {
            return false;
        }

        switch (provider.toLowerCase()) {
            case "google":
                return google.isValid();
            case "kakao":
                return kakao.isValid();
            default:
                return false;
        }
    }
}
