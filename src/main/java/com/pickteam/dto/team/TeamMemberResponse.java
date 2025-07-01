package com.pickteam.dto.team;

import com.pickteam.domain.team.TeamMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 팀 멤버 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberResponse {
    
    private Long id;
    private Long accountId;
    private String name;
    private String email;
    private String profileImage;
    private TeamMember.TeamRole teamRole;
    private TeamMember.TeamStatus teamStatus;
    private LocalDateTime joinedAt;
} 