package com.pickteam.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 비밀번호 재설정 관련 DTO 모음
 * - 비밀번호 찾기 기능에 필요한 모든 DTO를 한 파일에 정의
 */
public class PasswordResetDto {

    /**
     * 비밀번호 재설정 이메일 발송 요청 DTO
     */
    @Data
    public static class SendPasswordResetEmailRequest {
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효한 이메일 형식이어야 합니다.")
        private String email;
    }

    /**
     * 비밀번호 재설정 코드 확인 요청 DTO
     */
    @Data
    public static class VerifyResetCodeRequest {
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효한 이메일 형식이어야 합니다.")
        private String email;

        @NotBlank(message = "재설정 코드는 필수입니다.")
        @Pattern(regexp = "^[0-9]{6}$", message = "재설정 코드는 6자리 숫자여야 합니다.")
        private String resetCode;
    }

    /**
     * 비밀번호 재설정 코드 확인 응답 DTO
     */
    @Data
    public static class VerifyResetCodeResponse {
        /**
         * 요청 처리 성공 여부
         */
        private boolean success;

        /**
         * 재설정 코드의 유효성
         */
        private boolean valid;

        /**
         * 응답 메시지
         */
        private String message;
    }

    /**
     * 비밀번호 재설정 요청 DTO
     */
    @Data
    public static class ResetPasswordRequest {
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효한 이메일 형식이어야 합니다.")
        private String email;

        @NotBlank(message = "재설정 코드는 필수입니다.")
        @Pattern(regexp = "^[0-9]{6}$", message = "재설정 코드는 6자리 숫자여야 합니다.")
        private String resetCode;

        @NotBlank(message = "새 비밀번호는 필수입니다.")
        @Size(min = 8, max = 100, message = "새 비밀번호는 8자 이상 100자 이하여야 합니다.")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$", message = "새 비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
        private String newPassword;
    }
}
