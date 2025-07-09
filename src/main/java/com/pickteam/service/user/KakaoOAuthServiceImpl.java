package com.pickteam.service.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pickteam.config.OAuthConfig;
import com.pickteam.dto.user.KakaoUserInfo;
import com.pickteam.dto.user.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 카카오 OAuth 서비스 구현체
 * - 카카오 OAuth 2.0 API 연동
 * - Access Token 발급 및 사용자 정보 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOAuthServiceImpl implements KakaoOAuthService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OAuthConfig oauthConfig;

    // 카카오 OAuth 엔드포인트
    private static final String KAKAO_AUTH_URL = "https://kauth.kakao.com/oauth/authorize";
    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    @Override
    public String generateOAuthUrl() {
        log.debug("카카오 OAuth URL 생성 시작");

        String state = generateState(); // CSRF 방지용 state 값 생성

        String oauthUrl = UriComponentsBuilder.fromUriString(KAKAO_AUTH_URL)
                .queryParam("client_id", oauthConfig.getKakao().getClientId())
                .queryParam("redirect_uri", oauthConfig.getKakao().getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", "profile_nickname,profile_image")
                .queryParam("state", state)
                .build()
                .toUriString();

        log.info("카카오 OAuth URL 생성 완료: {}", maskUrl(oauthUrl));
        return oauthUrl;
    }

    @Override
    public String getAccessToken(String code) {
        log.info("카카오 Access Token 요청 시작");

        try {
            // 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // 요청 바디 설정
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", oauthConfig.getKakao().getClientId());
            params.add("client_secret", oauthConfig.getKakao().getClientSecret());
            params.add("redirect_uri", oauthConfig.getKakao().getRedirectUri());
            params.add("code", code);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            // Kakao Token API 호출
            ResponseEntity<String> response = restTemplate.exchange(
                    KAKAO_TOKEN_URL,
                    HttpMethod.POST,
                    request,
                    String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode tokenResponse = objectMapper.readTree(response.getBody());
                String accessToken = tokenResponse.get("access_token").asText();

                log.info("카카오 Access Token 발급 성공");
                return accessToken;
            } else {
                log.error("카카오 Access Token 발급 실패: HTTP {}", response.getStatusCode());
                throw new RuntimeException("카카오 Access Token 발급 실패: " + response.getStatusCode());
            }

        } catch (JsonProcessingException e) {
            log.error("카카오 Token 응답 파싱 실패", e);
            throw new RuntimeException("카카오 Token 응답 파싱 실패", e);
        } catch (Exception e) {
            log.error("카카오 Access Token 요청 중 오류 발생", e);
            throw new RuntimeException("카카오 Access Token 요청 실패", e);
        }
    }

    @Override
    public KakaoUserInfo getKakaoUserInfo(String accessToken) {
        log.info("카카오 사용자 정보 조회 시작");

        try {
            // 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(headers);

            // Kakao UserInfo API 호출
            ResponseEntity<KakaoUserInfo> response = restTemplate.exchange(
                    KAKAO_USER_INFO_URL,
                    HttpMethod.GET,
                    request,
                    KakaoUserInfo.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                KakaoUserInfo userInfo = response.getBody();

                if (userInfo.isValid()) {
                    log.info("카카오 사용자 정보 조회 성공 - ID: {}", userInfo.getId());
                    return userInfo;
                } else {
                    log.error("카카오 사용자 정보가 유효하지 않음");
                    throw new RuntimeException("카카오 사용자 정보가 유효하지 않습니다");
                }
            } else {
                log.error("카카오 사용자 정보 조회 실패: HTTP {}", response.getStatusCode());
                throw new RuntimeException("카카오 사용자 정보 조회 실패: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("카카오 사용자 정보 조회 중 오류 발생", e);
            throw new RuntimeException("카카오 사용자 정보 조회 실패", e);
        }
    }

    @Override
    public OAuthUserInfo getUserInfo(String accessToken) {
        KakaoUserInfo kakaoUserInfo = getKakaoUserInfo(accessToken);
        return kakaoUserInfo.toOAuthUserInfo();
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
