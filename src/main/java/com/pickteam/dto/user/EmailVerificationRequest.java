package com.pickteam.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 이메일 인증 요청 DTO
 * - 회원가입 시 이메일 주소 소유 여부 확인을 위한 인증 코드 발송 요청
 * - 지정된 이메일로 시간 제한이 있는 인증 코드 전송
 */
@Data
public class EmailVerificationRequest {
    /** 인증 코드를 받을 이메일 주소 */
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;
}
