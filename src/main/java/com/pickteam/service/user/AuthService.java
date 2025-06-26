package com.pickteam.service.user;

import com.pickteam.dto.user.UserLoginRequest;
import com.pickteam.dto.security.JwtAuthenticationResponse;
import com.pickteam.dto.security.RefreshTokenRequest;

public interface AuthService {

    // 로그인 인증
    JwtAuthenticationResponse authenticate(UserLoginRequest request);

    // 비밀번호 암호화
    String encryptPassword(String password);

    // 비밀번호 검증
    boolean matchesPassword(String rawPassword, String encodedPassword);

    // JWT 토큰 생성
    String generateAccessToken(Long userId, String email);

    // 리프레시 토큰 생성
    String generateRefreshToken(Long userId);

    // JWT 토큰 검증
    boolean validateToken(String token);

    // 리프레시 토큰으로 새 토큰 발급
    JwtAuthenticationResponse refreshToken(RefreshTokenRequest request);

    // 현재 로그인된 사용자 ID 가져오기
    Long getCurrentUserId();
}
