package com.pickteam.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

/**
 * 비밀번호 변경 요청 DTO
 * - 로그인된 사용자의 비밀번호 변경 시 사용
 * - 보안을 위해 현재 비밀번호 확인 후 새 비밀번호로 변경
 */
@Data
public class ChangePasswordRequest {
    /** 현재 비밀번호 (본인 확인용) */
    @ToString.Exclude
    @NotBlank(message = "현재 비밀번호는 필수입니다")
    private String currentPassword;

    /** 새로운 비밀번호 (암호화되어 저장됨) */
    @ToString.Exclude
    @NotBlank(message = "새 비밀번호는 필수입니다")
    @Size(min = 8, max = 50, message = "새 비밀번호는 8자 이상 50자 이하여야 합니다")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()\\-=]).*$", message = "새 비밀번호는 대소문자, 숫자, 특수문자를 모두 포함해야 합니다")
    private String newPassword;
}
