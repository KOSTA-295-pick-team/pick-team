package com.pickteam.dto.workspace;

import lombok.Data;

@Data
public class WorkspaceUpdateRequest {
    
    private String name;
    private String iconUrl;  
    private String password;
} 