package com.pickteam.dto.workspace;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class WorkspaceCreateRequest {
    
    @NotBlank(message = "워크스페이스 이름은 필수입니다")
    @Size(min = 1, max = 50, message = "워크스페이스 이름은 1자 이상 50자 이하여야 합니다")
    private String name;
    
    @Size(max = 10, message = "아이콘은 10자 이하여야 합니다")
    private String iconUrl;
    
    @Size(min = 4, max = 100, message = "비밀번호는 4자 이상 100자 이하여야 합니다")
    private String password;
} 