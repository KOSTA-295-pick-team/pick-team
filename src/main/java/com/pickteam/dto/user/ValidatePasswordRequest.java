package com.pickteam.dto.user;

import lombok.Data;

/**
 * 비밀번호 유효성 검사 요청 DTO
 * - 회원가입 시 비밀번호 정책 준수 여부 확인
 * - 클라이언트에서 실시간 유효성 검사를 위한 서버 검증
 * - 보안 정책: 최소 길이, 특수문자 포함, 복잡도 등 검증
 */
@Data
public class ValidatePasswordRequest {
    /** 유효성 검사할 비밀번호 */
    private String password;
}
