package com.pickteam.dto.user;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * OAuth 임시 코드 교환 요청 DTO
 * 
 * @author Pick Team
 * @since 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TempCodeRequest {

    /**
     * OAuth 인증 완료 후 발급된 임시 코드
     */
    private String tempCode;
}
