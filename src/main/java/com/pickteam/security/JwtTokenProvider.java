package com.pickteam.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰 제공자
 * - JWT Access Token과 Refresh Token 생성 및 검증
 * - 토큰에서 사용자 정보 추출
 * - 토큰 유효성 검사 및 만료 시간 관리
 */
@Component
@Slf4j
public class JwtTokenProvider {

    /** JWT 서명에 사용할 비밀키 */
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /** Access Token 만료 시간 (기본: 1시간) */
    @Value("${app.jwt.expiration:3600000}")
    private long jwtExpirationMs;

    /** Refresh Token 만료 시간 (기본: 24시간) */
    @Value("${app.jwt.refresh-expiration:86400000}")
    private long refreshExpirationMs;

    /** JWT 서명용 SecretKey 생성 */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /** Authentication 객체로부터 Access Token 생성 */
    public String generateAccessToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return generateAccessToken(userPrincipal.getId(), userPrincipal.getEmail());
    }

    /** 사용자 ID와 이메일로 Access Token 생성 */
    public String generateAccessToken(Long userId, String email) {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationMs);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /** 사용자 ID로 Refresh Token 생성 */
    public String generateRefreshToken(Long userId) {
        Date expiryDate = new Date(System.currentTimeMillis() + refreshExpirationMs);

        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /** 토큰에서 사용자 ID 추출 */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.parseLong(claims.getSubject());
    }

    /** 토큰에서 이메일 추출 */
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("email", String.class);
    }

    /**
     * 토큰 유효성 검사
     * - 토큰 서명, 만료시간, 형식 등을 종합적으로 검증
     * - 각종 JWT 예외 상황을 로깅하여 디버깅 지원
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException e) {
            log.warn("잘못된 형식의 JWT 토큰이 감지되었습니다");
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰이 감지되었습니다");
        } catch (UnsupportedJwtException e) {
            log.warn("지원하지 않는 JWT 토큰이 감지되었습니다");
        } catch (IllegalArgumentException e) {
            log.warn("비어있는 JWT 토큰이 감지되었습니다");
        }
        return false;
    }

    /** 토큰에서 만료 시간 추출 */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getExpiration();
    }

    /** Access Token 만료 시간 반환 (밀리초) */
    public long getJwtExpirationMs() {
        return jwtExpirationMs;
    }

    /** Refresh Token 만료 시간 반환 (밀리초) */
    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }
}
