package com.pickteam.dto.security;

import com.pickteam.dto.user.UserProfileResponse;

import lombok.Data;

@Data
public class JwtAuthenticationResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserProfileResponse user;

    public JwtAuthenticationResponse(String accessToken, String refreshToken, Long expiresIn,
            UserProfileResponse user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }
}
