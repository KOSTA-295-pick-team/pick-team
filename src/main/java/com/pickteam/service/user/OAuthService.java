package com.pickteam.service.user;

import com.pickteam.domain.enums.AuthProvider;
import com.pickteam.dto.security.JwtAuthenticationResponse;

/**
 * OAuth 통합 서비스 인터페이스
 * - 다양한 OAuth 제공자를 통합 관리
 * - OAuth 로그인 플로우 처리 및 JWT 토큰 발급
 * - 계정 연동/해제 기능 제공
 */
public interface OAuthService {

    /**
     * OAuth 로그인 URL 생성
     * 
     * @param provider OAuth 제공자 (GOOGLE, KAKAO)
     * @return OAuth 인증 URL
     * @throws IllegalArgumentException 지원하지 않는 제공자인 경우
     */
    String generateOAuthUrl(AuthProvider provider);

    /**
     * OAuth 로그인 처리 (자동 회원가입 포함)
     * - 신규 사용자: 자동 회원가입 후 로그인
     * - 기존 사용자: 바로 로그인
     * 
     * @param provider OAuth 제공자
     * @param code     Authorization Code
     * @return JWT 토큰 응답 (Access Token + Refresh Token)
     * @throws RuntimeException OAuth 인증 실패 또는 사용자 정보 조회 실패 시
     */
    JwtAuthenticationResponse processOAuthLogin(AuthProvider provider, String code);

    /**
     * 기존 계정에 OAuth 계정 연동
     * 
     * @param userId   연동할 사용자 ID
     * @param provider OAuth 제공자
     * @param code     Authorization Code
     * @throws RuntimeException 이미 연동된 계정이거나 연동 실패 시
     */
    void linkOAuthAccount(Long userId, AuthProvider provider, String code);

    /**
     * OAuth 계정 연동 해제
     * 
     * @param userId   사용자 ID
     * @param provider OAuth 제공자
     * @throws RuntimeException 연동되지 않은 계정이거나 해제 실패 시
     */
    void unlinkOAuthAccount(Long userId, AuthProvider provider);

    /**
     * 사용자의 OAuth 연동 상태 확인
     * 
     * @param userId   사용자 ID
     * @param provider OAuth 제공자
     * @return 연동 상태 (true: 연동됨, false: 연동 안됨)
     */
    boolean isOAuthLinked(Long userId, AuthProvider provider);

    /**
     * 삭제된 계정 정보 조회 (OAuth 로그인 실패 시 상세 정보 제공용)
     * 
     * @param accountId 삭제된 계정 ID
     * @throws OAuthDeletedAccountException 항상 발생 (삭제된 계정 정보와 함께)
     * @throws RuntimeException             계정을 찾을 수 없거나 삭제되지 않은 계정인 경우
     */
    void getDeletedAccountInfo(Long accountId);
}
