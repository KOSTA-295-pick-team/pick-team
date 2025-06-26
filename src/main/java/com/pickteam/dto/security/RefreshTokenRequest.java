package com.pickteam.dto.security;

import lombok.Data;

@Data
public class RefreshTokenRequest {
    private String refreshToken;
}
