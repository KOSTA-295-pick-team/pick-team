package com.pickteam.dto.team;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 팀 수정 요청 DTO
 */
@Data
public class TeamUpdateRequest {
    
    @Size(min = 1, max = 50, message = "팀 이름은 1자 이상 50자 이하여야 합니다.")
    private String name;
} 