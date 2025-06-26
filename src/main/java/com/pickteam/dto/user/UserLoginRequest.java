package com.pickteam.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

/**
 * 로그인 요청 DTO
 * - 사용자 인증을 위한 로그인 정보
 * - JWT 토큰 발급을 위한 사전 인증 단계
 */
@Data
public class UserLoginRequest {
    /** 사용자 이메일 (로그인 ID) */
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    /** 사용자 비밀번호 (평문으로 전송, 서버에서 암호화된 값과 비교) */
    @ToString.Exclude
    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;
}
