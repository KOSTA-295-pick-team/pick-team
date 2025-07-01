package com.pickteam.dto.workspace;

import com.pickteam.dto.user.UserSummaryResponse;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class WorkspaceResponse {
    
    private Long id;
    private String name;
    private String iconUrl;
    private UserSummaryResponse owner;
    private boolean passwordProtected;
    private String inviteCode;
    private String url; // 초대 링크용 URL
    private int memberCount;
    private List<UserSummaryResponse> members;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 