package com.pickteam.service;

import com.pickteam.domain.user.Account;
import com.pickteam.domain.videochat.VideoChannel;
import com.pickteam.domain.videochat.VideoMember;
import com.pickteam.domain.workspace.Workspace;
import com.pickteam.dto.VideoChannelDTO;
import com.pickteam.dto.VideoMemberDTO;
import com.pickteam.exception.VideoConferenceErrorCode;
import com.pickteam.exception.VideoConferenceException;
import com.pickteam.repository.VideoChannelRepository;
import com.pickteam.repository.VideoMemberRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoConferenceServiceImpl implements VideoConferenceService {


    private final VideoChannelRepository videoChannelRepository;

    private final VideoMemberRepository videoMemberRepository;

    private ModelMapper modelMapper = new ModelMapper();

    @Transactional(readOnly = true)
    @Override
    public List<VideoChannelDTO> selectVideoChannels(Long workspaceId, Long accountId) throws VideoConferenceException {
        List<VideoChannel> channels = new ArrayList<>();
        if (accountId == null) {
            channels = videoChannelRepository.selectChannelsByWorkSpaceId(workspaceId);
        } else {
            channels = videoChannelRepository.selectChannelsByWorkSpaceId(workspaceId, accountId);
        }
        if (channels.isEmpty()) {
            throw new VideoConferenceException(VideoConferenceErrorCode.CHANNELS_NOT_FOUND);
        }

        return channels.stream().map(channel -> modelMapper.map(channel, VideoChannelDTO.class)).toList();

    }

    @Transactional
    @Override
    public void insertVideoChannel(Long workspaceId, String videoChannelName) {
        Workspace workspace = Workspace.builder().id(workspaceId).build();

        videoChannelRepository.save(VideoChannel.builder().name(videoChannelName).workspace(workspace).build());

    }

    @Transactional
    @Override
    public void joinVideoChannel(Long accountId, Long videoChannelId) {

        videoMemberRepository.save(VideoMember.builder().account(Account.builder().id(accountId).build()).
                videoChannel(VideoChannel.builder().id(videoChannelId).id(videoChannelId).build()).build());

    }

    @Transactional
    @Override
    public void deleteVideoChannel(Long videoChannelId) throws VideoConferenceException {

        VideoChannel channel = videoChannelRepository.findById(videoChannelId).orElseThrow(() -> new VideoConferenceException(VideoConferenceErrorCode.CHANNEL_NOT_FOUND));

        videoChannelRepository.delete(channel);

    }

    @Transactional(readOnly = true)
    @Override
    public List<VideoMemberDTO> selectVideoChannelParticipants(Long videoChannelId) throws VideoConferenceException {

        List<VideoMember> members = videoMemberRepository.selectAccountsByChannelId(videoChannelId);

        if (members.isEmpty()) {
            throw new VideoConferenceException(VideoConferenceErrorCode.MEMBERS_NOT_FOUND);
        }

        return members.stream().map(member -> {
            VideoMemberDTO memberDTO = modelMapper.map(member.getAccount(), VideoMemberDTO.class);
            memberDTO.setJoinDate(member.getCreatedAt());
            memberDTO.setUserId(member.getAccount().getId());
            return memberDTO;
        }).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void deleteVideoChannelParticipant(Long memberId) throws VideoConferenceException {

      VideoMember member = videoMemberRepository.findById(memberId).orElseThrow(()->new VideoConferenceException(VideoConferenceErrorCode.MEMBER_NOT_FOUND));

      videoMemberRepository.delete(member);

    }
}
