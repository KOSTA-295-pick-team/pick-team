package com.pickteam.dto.team;

import lombok.Builder;
import lombok.Getter;

/**
 * teamResponse DTO
 * 팀 정보 관련 응답에 사용되는 DTO
 * 팀 id, 팀명, 팀장 ID, 워크스페이스 id, 워크스페이스 명
 */
@Getter
@Builder
public class TeamResponse {
    private Long id;
    private String name;
    private Long ownerId;
    private Long workspaceId;
    private String workspaceName;

}