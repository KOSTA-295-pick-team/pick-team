package com.pickteam.dto.team;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 팀 생성 요청 DTO
 */
@Data
public class TeamCreateRequest {
    
    @NotBlank(message = "팀 이름은 필수입니다.")
    @Size(min = 1, max = 50, message = "팀 이름은 1자 이상 50자 이하여야 합니다.")
    private String name;
    
    @NotNull(message = "워크스페이스 ID는 필수입니다.")
    private Long workspaceId;
} 