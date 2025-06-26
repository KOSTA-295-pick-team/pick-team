package com.pickteam.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 이메일 인증 확인 요청 DTO
 * - 사용자가 이메일로 받은 인증 코드를 입력하여 이메일 소유권 확인
 * - 인증 코드 만료 시간 내에 올바른 코드 입력 시 인증 완료 처리
 */
@Data
public class EmailVerificationConfirmRequest {
    /** 인증 대상 이메일 주소 */
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    /** 이메일로 받은 인증 코드 */
    @NotBlank(message = "인증 코드는 필수입니다")
    @Pattern(regexp = "^[0-9]{6}$", message = "인증 코드는 6자리 숫자여야 합니다")
    private String verificationCode;
}
