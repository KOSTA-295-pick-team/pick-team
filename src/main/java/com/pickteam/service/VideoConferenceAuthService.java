package com.pickteam.service;

import jakarta.servlet.http.HttpServletRequest;

public interface VideoConferenceAuthService {
    boolean canViewChannels(Long userId, Long workSpaceId);

    boolean canCreateChannel(Long accountId, Long workSpaceId);

    boolean canJoinChannel(Long accountId, Long workSpaceId);

    boolean canLeaveChannel(Long accountId, Long channelId, Long memberId);

    boolean canViewParticipants(Long accountId, Long channelId);

    boolean canJoinConference(Long accountId, Long channelId);

    boolean canCallLiveKitWebhook(HttpServletRequest request);

    boolean isWorkspaceAdmin(Long accountId, Long workspaceId);
}
