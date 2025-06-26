package com.pickteam.dto.workspace;

import lombok.Data;

@Data
public class WorkspaceJoinRequest {
    
    private String inviteCode;
    private String password;
} 