package com.pickteam.dto.user;

import lombok.Data;

/**
 * 비밀번호 변경 요청 DTO
 * - 로그인된 사용자의 비밀번호 변경 시 사용
 * - 보안을 위해 현재 비밀번호 확인 후 새 비밀번호로 변경
 */
@Data
public class ChangePasswordRequest {
    /** 현재 비밀번호 (본인 확인용) */
    private String currentPassword;

    /** 새로운 비밀번호 (암호화되어 저장됨) */
    private String newPassword;
}
