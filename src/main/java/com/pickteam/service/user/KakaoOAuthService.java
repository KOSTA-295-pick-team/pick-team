package com.pickteam.service.user;

import com.pickteam.dto.user.KakaoUserInfo;
import com.pickteam.dto.user.OAuthUserInfo;

/**
 * 카카오 OAuth 서비스 인터페이스
 * - 카카오 OAuth 2.0 인증 플로우 처리
 * - Access Token 발급 및 사용자 정보 조회
 */
public interface KakaoOAuthService {

    /**
     * Authorization Code를 사용하여 Access Token 발급
     * 
     * @param code OAuth Authorization Code
     * @return Access Token
     * @throws RuntimeException OAuth 인증 실패 시
     */
    String getAccessToken(String code);

    /**
     * Access Token을 사용하여 카카오 사용자 정보 조회
     * 
     * @param accessToken 카카오 Access Token
     * @return 카카오 사용자 정보
     * @throws RuntimeException 사용자 정보 조회 실패 시
     */
    KakaoUserInfo getKakaoUserInfo(String accessToken);

    /**
     * Access Token을 사용하여 표준화된 사용자 정보 조회
     * 
     * @param accessToken 카카오 Access Token
     * @return 표준화된 OAuth 사용자 정보
     */
    OAuthUserInfo getUserInfo(String accessToken);

    /**
     * 카카오 OAuth 로그인 URL 생성
     * 
     * @return 카카오 OAuth 인증 URL
     */
    String generateOAuthUrl();
}
