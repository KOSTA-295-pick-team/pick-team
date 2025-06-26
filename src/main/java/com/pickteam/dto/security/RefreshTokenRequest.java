package com.pickteam.dto.security;

import lombok.Data;

/**
 * JWT Refresh Token 갱신 요청 DTO
 * - Access Token 만료 시 새로운 토큰 발급 요청에 사용
 */
@Data
public class RefreshTokenRequest {
    /** 갱신에 사용할 Refresh Token */
    private String refreshToken;
}
