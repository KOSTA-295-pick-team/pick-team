package com.pickteam.dto.user;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.ToString;

/**
 * 회원가입 요청 DTO
 * - 이메일과 비밀번호만으로 간소화된 회원가입
 * - 사용자 경험 개선을 위한 최소한의 필수 정보만 요구
 */
@Data
public class SignupRequest {

    /** 사용자 이메일 (로그인 ID) */
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    /** 비밀번호 (암호화되어 저장) */
    @ToString.Exclude
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 50, message = "비밀번호는 8자 이상 50자 이하여야 합니다")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()\\-=]).*$", message = "비밀번호는 대소문자, 숫자, 특수문자(!@#$%^&*()-=)를 모두 포함해야 합니다")
    private String password;

    /** 비밀번호 확인 */
    @ToString.Exclude
    @NotBlank(message = "비밀번호 확인은 필수입니다")
    private String confirmPassword;
}
