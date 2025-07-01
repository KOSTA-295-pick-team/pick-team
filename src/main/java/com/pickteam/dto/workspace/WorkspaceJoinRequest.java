package com.pickteam.dto.workspace;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class WorkspaceJoinRequest {
    
    @NotBlank(message = "초대코드는 필수입니다")
    @Size(min = 1, max = 100, message = "초대코드는 1자 이상 100자 이하여야 합니다")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "초대코드는 영문자, 숫자, 하이픈(-), 언더스코어(_)만 포함할 수 있습니다")
    private String inviteCode;
    
    @Size(min = 4, max = 100, message = "비밀번호는 4자 이상 100자 이하여야 합니다")
    private String password;
} 