package com.pickteam.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 해시태그 추가 요청 DTO
 * - 사용자가 새로운 해시태그를 추가할 때 사용
 */
@Data
public class HashtagAddRequest {

    /** 해시태그 이름 */
    @NotBlank(message = "해시태그 이름은 필수입니다")
    @Size(min = 1, max = 20, message = "해시태그는 1자 이상 20자 이하여야 합니다")
    private String name;
}
