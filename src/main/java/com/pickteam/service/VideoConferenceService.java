package com.pickteam.service;

import com.pickteam.dto.VideoChannelDTO;
import com.pickteam.dto.VideoMemberDTO;
import com.pickteam.exception.VideoConferenceException;

import java.util.List;

public interface VideoConferenceService {

    public List<VideoChannelDTO> selectVideoChannels(Long workspaceId, Long accountId) throws VideoConferenceException;

    public void insertVideoChannel(Long workspaceId, String videoChannelName);

    public void joinVideoChannel(Long accountId, Long videoChannelId);

    public void deleteVideoChannel(Long videoChannelId) throws VideoConferenceException;

    public List<VideoMemberDTO> selectVideoChannelParticipants(Long videoChannelId) throws VideoConferenceException;

    public void deleteVideoChannelParticipant(Long memberId) throws VideoConferenceException;

}
