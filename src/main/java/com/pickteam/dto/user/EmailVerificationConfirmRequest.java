package com.pickteam.dto.user;

import lombok.Data;

@Data
public class EmailVerificationConfirmRequest {
    private String email;
    private String verificationCode;
}
