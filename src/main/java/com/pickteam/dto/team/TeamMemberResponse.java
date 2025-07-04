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
    
    // 사용자 상세 정보 추가
    private Integer age;
    private String mbti;
    private String disposition;
    private String introduction;
    private String portfolio;
    private String preferWorkstyle;
    private String dislikeWorkstyle;
    private String likes;
    private String dislikes;
} 