package com.pickteam.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * ID 중복 검사 요청 DTO
 * - 회원가입 시 이메일 주소 중복 여부 확인
 * - 사용자가 입력한 이메일이 이미 등록된 계정에서 사용 중인지 검증
 */
@Data
public class CheckDuplicateIdRequest {
    /** 중복 검사할 이메일 주소 */
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;
}
