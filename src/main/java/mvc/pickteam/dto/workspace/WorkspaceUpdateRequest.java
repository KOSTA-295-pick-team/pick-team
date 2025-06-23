package mvc.pickteam.dto.workspace;

import lombok.Data;

import jakarta.validation.constraints.Size;

@Data
public class WorkspaceUpdateRequest {
    
    @Size(max = 100, message = "워크스페이스 이름은 100자 이내여야 합니다.")
    private String name;
    
    private String iconUrl;
    
    private String password;
} 