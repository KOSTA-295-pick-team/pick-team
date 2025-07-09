package com.pickteam.service;

import jakarta.servlet.http.HttpServletRequest;

public interface VideoConferenceAuthService {
    boolean canViewChannels(Long userId, Long WorkSpaceId);

    boolean canCreateChannel(Long accountId,Long WorkSpaceId);

    boolean canJoinChannel(Long accountId,Long WorkSpaceId);

    boolean canLeaveChannel(Long accountId,Long channelId,Long memberId);

    boolean canViewParticipants(Long accountId,Long channelId);

    boolean canJoInConference(Long accountId,Long channelId);

    boolean canCallLiveKitWebhook(HttpServletRequest request);

}
