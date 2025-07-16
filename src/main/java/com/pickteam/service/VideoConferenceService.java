package com.pickteam.service;

import com.pickteam.dto.VideoChannelDTO;
import com.pickteam.dto.VideoMemberDTO;
import com.pickteam.exception.VideoConferenceException;
import com.pickteam.util.VideoConferenceControlMsg;

import java.util.List;

public interface VideoConferenceService {

    public List<VideoChannelDTO> selectVideoChannels(Long workspaceId) throws VideoConferenceException;

    public VideoChannelDTO selectVideoChannel(Long channelId) throws VideoConferenceException;

    public VideoChannelDTO insertVideoChannel(Long workspaceId, String videoChannelName) throws VideoConferenceException;

    public void joinVideoChannel(Long accountId, Long videoChannelId) throws VideoConferenceException;

    public void deleteVideoChannel(Long videoChannelId) throws VideoConferenceException;

    public List<VideoMemberDTO> selectVideoChannelParticipants(Long videoChannelId) throws VideoConferenceException;

    public void deleteVideoChannelParticipant(Long memberId, Long channelId) throws VideoConferenceException;

    public boolean checkUserInVideoChannel(Long accountId, Long videoChannelId);

    public String joinVideoConferenceRoom(Long accountId, Long videoChannelId, String username, String userEmail) throws VideoConferenceException;

    public void handleVideoConferenceEvent(String userEmail, Long roomId, VideoConferenceControlMsg event);

    public void handleLiveKitHookEvent(String authHeader,String body) throws Exception;

}
