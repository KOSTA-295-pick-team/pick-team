package com.pickteam.dto.team;

import com.pickteam.dto.user.UserSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 팀 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamResponse {
    
    private Long id;
    private String name;
    private Long workspaceId;
    private String workspaceName;
    private UserSummaryResponse leader;
    private int memberCount;
    private List<TeamMemberResponse> members;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 