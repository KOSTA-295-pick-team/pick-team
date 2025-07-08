package com.pickteam.service.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pickteam.dto.user.GoogleUserInfo;
import com.pickteam.dto.user.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 구글 OAuth 서비스 구현체
 * - 구글 OAuth 2.0 API 연동
 * - Access Token 발급 및 사용자 정보 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleOAuthServiceImpl implements GoogleOAuthService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 구글 OAuth 설정값
    @Value("${app.oauth.google.client-id}")
    private String clientId;

    @Value("${app.oauth.google.client-secret}")
    private String clientSecret;

    @Value("${app.oauth.google.redirect-uri}")
    private String redirectUri;

    // 구글 OAuth 엔드포인트
    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    @Override
    public String generateOAuthUrl() {
        log.debug("구글 OAuth URL 생성 시작");

        String state = generateState(); // CSRF 방지용 state 값 생성

        String oauthUrl = UriComponentsBuilder.fromUriString(GOOGLE_AUTH_URL)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "openid email profile")
                .queryParam("state", state)
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .build()
                .toUriString();

        log.info("구글 OAuth URL 생성 완료: {}", maskUrl(oauthUrl));
        return oauthUrl;
    }

    @Override
    public String getAccessToken(String code) {
        log.info("구글 Access Token 요청 시작");

        try {
            // 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // 요청 바디 설정
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("code", code);
            params.add("grant_type", "authorization_code");
            params.add("redirect_uri", redirectUri);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            // Google Token API 호출
            ResponseEntity<String> response = restTemplate.exchange(
                    GOOGLE_TOKEN_URL,
                    HttpMethod.POST,
                    request,
                    String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode tokenResponse = objectMapper.readTree(response.getBody());
                String accessToken = tokenResponse.get("access_token").asText();

                log.info("구글 Access Token 발급 성공");
                return accessToken;
            } else {
                log.error("구글 Access Token 발급 실패: HTTP {}", response.getStatusCode());
                throw new RuntimeException("구글 Access Token 발급 실패: " + response.getStatusCode());
            }

        } catch (JsonProcessingException e) {
            log.error("구글 Token 응답 파싱 실패", e);
            throw new RuntimeException("구글 Token 응답 파싱 실패", e);
        } catch (Exception e) {
            log.error("구글 Access Token 요청 중 오류 발생", e);
            throw new RuntimeException("구글 Access Token 요청 실패", e);
        }
    }

    @Override
    public GoogleUserInfo getGoogleUserInfo(String accessToken) {
        log.info("구글 사용자 정보 조회 시작");

        try {
            // 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(headers);

            // Google UserInfo API 호출
            ResponseEntity<GoogleUserInfo> response = restTemplate.exchange(
                    GOOGLE_USER_INFO_URL,
                    HttpMethod.GET,
                    request,
                    GoogleUserInfo.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                GoogleUserInfo userInfo = response.getBody();

                if (userInfo.isValid()) {
                    log.info("구글 사용자 정보 조회 성공 - 이메일: {}", maskEmail(userInfo.getEmail()));
                    return userInfo;
                } else {
                    log.error("구글 사용자 정보가 유효하지 않음");
                    throw new RuntimeException("구글 사용자 정보가 유효하지 않습니다");
                }
            } else {
                log.error("구글 사용자 정보 조회 실패: HTTP {}", response.getStatusCode());
                throw new RuntimeException("구글 사용자 정보 조회 실패: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("구글 사용자 정보 조회 중 오류 발생", e);
            throw new RuntimeException("구글 사용자 정보 조회 실패", e);
        }
    }

    @Override
    public OAuthUserInfo getUserInfo(String accessToken) {
        GoogleUserInfo googleUserInfo = getGoogleUserInfo(accessToken);
        return googleUserInfo.toOAuthUserInfo();
    }

    /**
     * CSRF 방지용 state 값 생성
     * 
     * @return 랜덤 state 문자열
     */
    private String generateState() {
        return java.util.UUID.randomUUID().toString();
    }

    /**
     * 이메일 주소 마스킹 (로그용)
     * 
     * @param email 원본 이메일
     * @return 마스킹된 이메일
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        if (localPart.length() <= 2) {
            return "***@" + domain;
        }

        return localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1) + "@" + domain;
    }

    /**
     * URL 마스킹 (로그용)
     * 
     * @param url 원본 URL
     * @return 마스킹된 URL
     */
    private String maskUrl(String url) {
        if (url == null) {
            return "***";
        }

        // client_id 파라미터 마스킹
        return url.replaceAll("client_id=[^&]+", "client_id=***");
    }
}
