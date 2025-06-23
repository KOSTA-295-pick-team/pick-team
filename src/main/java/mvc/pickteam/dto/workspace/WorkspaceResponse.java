package mvc.pickteam.dto.workspace;

import lombok.Builder;
import lombok.Data;
import mvc.pickteam.dto.user.UserSummaryResponse;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class WorkspaceResponse {
    
    private Long id;
    private String name;
    private String iconUrl;
    private Long ownerId;
    private String inviteCode;
    private boolean hasPassword;
    private LocalDateTime createdAt;
    private List<UserSummaryResponse> members;
} 