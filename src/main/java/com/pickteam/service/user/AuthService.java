package com.pickteam.service.user;

import com.pickteam.dto.user.UserLoginRequest;
import com.pickteam.dto.user.LogoutResponse;
import com.pickteam.dto.user.SessionInfoRequest;
import com.pickteam.dto.security.JwtAuthenticationResponse;
import com.pickteam.dto.security.RefreshTokenRequest;
import com.pickteam.exception.user.UserNotFoundException;
import org.springframework.security.core.AuthenticationException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 인증 서비스 인터페이스
 * - JWT 기반 사용자 인증 및 권한 관리
 * - 비밀번호 암호화 및 검증
 * - 토큰 생성, 갱신, 검증 등 핵심 보안 기능 제공
 * - 세션 정보 및 보안 로깅 지원
 */
public interface AuthService {

        /**
         * 사용자 로그인 인증을 처리합니다.
         * 이메일과 비밀번호를 검증하고, 인증 성공 시 JWT 토큰을 발급합니다.
         *
         * @param request 이메일과 비밀번호를 포함한 로그인 요청
         * @return Access Token, Refresh Token, 사용자 정보를 포함한 JWT 인증 응답
         * @throws AuthenticationException 인증에 실패한 경우
         * @throws UserNotFoundException   사용자 계정을 찾을 수 없는 경우
         */
        JwtAuthenticationResponse authenticate(UserLoginRequest request);

        /**
         * 클라이언트 정보를 포함한 사용자 로그인 인증을 처리합니다.
         *
         * @param request     로그인 요청 정보
         * @param sessionInfo 클라이언트 세션 정보
         * @param httpRequest HTTP 요청 (IP, User-Agent 등 추출용)
         * @return JWT 인증 응답
         * @throws AuthenticationException 인증에 실패한 경우
         * @throws UserNotFoundException   사용자 계정을 찾을 수 없는 경우
         */
        JwtAuthenticationResponse authenticateWithClientInfo(UserLoginRequest request,
                        SessionInfoRequest sessionInfo,
                        HttpServletRequest httpRequest);

        /**
         * 평문 비밀번호를 안전하게 암호화합니다.
         * BCrypt 해시 알고리즘을 사용하여 단방향 암호화를 수행합니다.
         *
         * @param password 암호화할 평문 비밀번호
         * @return 암호화된 비밀번호 해시
         * @throws IllegalArgumentException 비밀번호가 null이거나 빈 문자열인 경우
         */
        String encryptPassword(String password);

        /**
         * 평문 비밀번호와 암호화된 비밀번호를 비교 검증합니다.
         *
         * @param rawPassword     평문 비밀번호
         * @param encodedPassword 암호화된 비밀번호
         * @return 비밀번호가 일치하면 true, 그렇지 않으면 false
         * @throws IllegalArgumentException 매개변수가 null인 경우
         */
        boolean matchesPassword(String rawPassword, String encodedPassword);

        /**
         * 사용자를 위한 JWT Access Token을 생성합니다 (이름 포함).
         *
         * @param userId 사용자 ID
         * @param email  사용자 이메일
         * @param name   사용자 이름
         * @return 생성된 Access Token
         * @throws IllegalArgumentException 매개변수가 유효하지 않은 경우
         */
        String generateAccessToken(Long userId, String email, String name);

        /**
         * 사용자를 위한 Refresh Token을 생성하고 저장합니다.
         *
         * @param userId 사용자 ID
         * @return 생성된 Refresh Token
         * @throws com.pickteam.exception.UserNotFoundException 사용자를 찾을 수 없는 경우
         * @throws IllegalArgumentException                     userId가 null인 경우
         */
        String generateRefreshToken(Long userId);

        /**
         * 클라이언트 정보를 포함한 Refresh Token을 생성하고 저장합니다.
         *
         * @param userId     사용자 ID
         * @param clientInfo 클라이언트 정보
         * @return 생성된 Refresh Token
         */
        String generateRefreshTokenWithClientInfo(Long userId,
                        com.pickteam.util.ClientInfoExtractor.ClientInfo clientInfo);

        /**
         * 클라이언트 정보를 포함한 Refresh Token을 생성하고 저장합니다.
         *
         * @param userId      사용자 ID
         * @param sessionInfo 세션 정보
         * @param httpRequest HTTP 요청
         * @return 생성된 Refresh Token
         */
        String generateRefreshTokenWithSessionInfo(Long userId, SessionInfoRequest sessionInfo,
                        HttpServletRequest httpRequest);

        /**
         * JWT 토큰의 유효성을 검증합니다.
         * 토큰의 서명, 만료 시간, 형식 등을 종합적으로 검증합니다.
         *
         * @param token 검증할 JWT 토큰
         * @return 유효한 토큰이면 true, 그렇지 않으면 false
         * @throws SecurityException        토큰 변조가 감지된 경우
         * @throws IllegalArgumentException 토큰이 null이거나 빈 문자열인 경우
         */
        boolean validateToken(String token);

        /**
         * Refresh Token을 사용하여 새로운 JWT 토큰 쌍을 발급합니다.
         * 기존 Refresh Token을 무효화하고 새로운 토큰을 생성합니다.
         *
         * @param request Refresh Token 요청 정보
         * @return 새로운 JWT 인증 응답 (새 Access Token, 새 Refresh Token)
         * @throws org.springframework.security.core.AuthenticationException Refresh
         *                                                                   Token이 유효하지
         *                                                                   않은 경우
         * @throws com.pickteam.exception.UserNotFoundException              사용자를 찾을 수
         *                                                                   없는 경우
         * @throws IllegalArgumentException                                  요청이 null이거나
         *                                                                   토큰이 없는 경우
         */
        JwtAuthenticationResponse refreshToken(RefreshTokenRequest request);

        /**
         * 현재 인증된 사용자의 ID를 가져옵니다.
         * Spring Security Context에서 인증 정보를 추출하여 사용자 ID를 반환합니다.
         *
         * @return 현재 사용자 ID
         * @throws org.springframework.security.core.AuthenticationException 인증되지 않은
         *                                                                   사용자인 경우
         * @throws IllegalStateException                                     SecurityContext에
         *                                                                   사용자 정보가 없는
         *                                                                   경우
         */
        Long getCurrentUserId();

        /**
         * 현재 사용자의 인증 상태를 확인하고 사용자 ID를 반환합니다.
         * 인증되지 않은 경우 UnauthorizedException을 발생시킵니다.
         *
         * @return 인증된 사용자 ID
         * @throws com.pickteam.exception.UnauthorizedException 인증되지 않은 사용자인 경우
         */
        Long requireAuthentication();

        /**
         * 사용자 로그아웃을 처리합니다.
         * 해당 사용자의 모든 Refresh Token을 무효화하여 보안을 강화합니다.
         *
         * @param userId 로그아웃할 사용자 ID
         * @throws UserNotFoundException    사용자를 찾을 수 없는 경우
         * @throws IllegalArgumentException userId가 null인 경우
         */
        void logout(Long userId);

        /**
         * 개선된 사용자 로그아웃을 처리합니다.
         * 로그아웃 시간과 무효화된 세션 수 등 상세 정보를 반환합니다.
         *
         * @param userId 로그아웃할 사용자 ID
         * @return 로그아웃 상세 정보
         * @throws UserNotFoundException    사용자를 찾을 수 없는 경우
         * @throws IllegalArgumentException userId가 null인 경우
         */
        LogoutResponse logoutWithDetails(Long userId);

        /**
         * 클라이언트 정보를 포함한 상세 로그아웃을 처리합니다.
         *
         * @param userId      로그아웃할 사용자 ID
         * @param httpRequest HTTP 요청 (IP 추출용)
         * @return 로그아웃 상세 정보
         */
        LogoutResponse logoutWithDetails(Long userId, HttpServletRequest httpRequest);
}
