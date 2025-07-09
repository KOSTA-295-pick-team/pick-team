package com.pickteam.service;

import com.pickteam.repository.VideoMemberRepository;
import com.pickteam.repository.workspace.WorkspaceMemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("videoConferenceAuthService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VideoConferenceAuthServiceImpl implements VideoConferenceAuthService {

    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final VideoMemberRepository videoMemberRepository;

    @Value("${LIVEKIT_SERVER_IP}")
    private String liveKitServerIp;

    @Override
    public boolean canViewChannels(Long accountId, Long WorkSpaceId) {
        return workspaceMemberRepository.existsActiveByWorkspaceIdAndAccountId(WorkSpaceId, accountId);
    }

    @Override
    public boolean canCreateChannel(Long accountId, Long WorkSpaceId) {
        return workspaceMemberRepository.existsActiveByWorkspaceIdAndAccountId(WorkSpaceId, accountId);
    }

    @Override
    public boolean canJoinChannel(Long accountId, Long WorkSpaceId) {
        return workspaceMemberRepository.existsActiveByWorkspaceIdAndAccountId(WorkSpaceId, accountId);
    }

    @Override
    public boolean canLeaveChannel(Long accountId, Long channelId,Long memberId ) {
        return videoMemberRepository.findByAccountIdAndVideoChannelId(accountId, channelId).getId().equals(memberId);
    }

    @Override
    public boolean canViewParticipants(Long accountId, Long channelId) {
        return videoMemberRepository.findByAccountIdAndVideoChannelId(accountId, channelId) != null;
    }

    @Override
    public boolean canJoInConference(Long accountId, Long channelId) {
        return videoMemberRepository.findByAccountIdAndVideoChannelId(accountId, channelId) != null;
    }

    @Override
    public boolean canCallLiveKitWebhook(HttpServletRequest request) {
        return liveKitServerIp.equals(request.getRemoteAddr());
    }
}
