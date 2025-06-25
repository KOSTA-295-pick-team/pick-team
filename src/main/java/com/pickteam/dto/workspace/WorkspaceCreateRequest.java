package com.pickteam.dto.workspace;

import lombok.Data;

@Data
public class WorkspaceCreateRequest {
    
    private String name;
    private String iconUrl;
    private String password;
} 