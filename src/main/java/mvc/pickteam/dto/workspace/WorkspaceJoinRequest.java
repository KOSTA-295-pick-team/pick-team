package mvc.pickteam.dto.workspace;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class WorkspaceJoinRequest {
    
    @NotBlank(message = "초대 코드는 필수입니다.")
    private String inviteCode;
    
    private String password;
} 