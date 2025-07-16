package com.pickteam.service;

import com.pickteam.domain.user.Account;
import com.pickteam.domain.videochat.VideoChannel;
import com.pickteam.domain.videochat.VideoMember;
import com.pickteam.domain.workspace.Workspace;
import com.pickteam.dto.*;
import com.pickteam.exception.VideoConferenceErrorCode;
import com.pickteam.exception.VideoConferenceException;
import com.pickteam.repository.VideoChannelRepository;
import com.pickteam.repository.VideoMemberRepository;
import com.pickteam.util.VideoConferenceControlMsg;
import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import io.livekit.server.WebhookReceiver;
import livekit.LivekitWebhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoConferenceServiceImpl implements VideoConferenceService {

    @Value("${livekit.api.key}")
    private String LIVEKIT_API_KEY;

    @Value("${livekit.api.secret}")
    private String LIVEKIT_API_SECRET;

    private final VideoChannelRepository videoChannelRepository;

    private final VideoMemberRepository videoMemberRepository;

    @Autowired
    private ModelMapper modelMapper;


    @Autowired
    @Lazy
    private SimpMessagingTemplate messagingTemplate;

    private Map<Long, String> currentSharingUser = new HashMap<>();

    private Map<Long, String> sharingWaitingUser = new HashMap<>();

    @Transactional(readOnly = true)
    @Override
    public List<VideoChannelDTO> selectVideoChannels(Long workspaceId) throws VideoConferenceException {

        List<VideoChannel> channels = videoChannelRepository.selectChannelsByWorkSpaceId(workspaceId);

        if (channels.isEmpty()) {
            throw new VideoConferenceException(VideoConferenceErrorCode.CHANNELS_NOT_FOUND);
        }

        return channels.stream().map(channel -> modelMapper.map(channel, VideoChannelDTO.class)).toList();

    }

    @Override
    public VideoChannelDTO selectVideoChannel(Long channelId) throws VideoConferenceException {
        VideoChannel vc = videoChannelRepository.findById(channelId).orElseThrow(() -> new VideoConferenceException(VideoConferenceErrorCode.CHANNEL_NOT_FOUND));
        if (vc.getIsDeleted()) {
            throw new VideoConferenceException(VideoConferenceErrorCode.CHANNEL_NOT_FOUND);
        }
        return modelMapper.map(vc, VideoChannelDTO.class);
    }

    @Transactional
    @Override
    public VideoChannelDTO insertVideoChannel(Long workspaceId, String videoChannelName) throws VideoConferenceException {
        VideoChannel vc = videoChannelRepository.findByName(videoChannelName);
        if(vc != null && !vc.getIsDeleted()) {
            throw new VideoConferenceException(VideoConferenceErrorCode.DUPLICATE_CHANNEL_NAME);
        }
        Workspace workspace = Workspace.builder().id(workspaceId).build();
        VideoChannel videochannel = VideoChannel.builder().name(videoChannelName).workspace(workspace).build();
        videoChannelRepository.save(videochannel);
        VideoChannelDTO videoChannelDTO = new VideoChannelDTO();
        videoChannelDTO.setName(videoChannelName);
        videoChannelDTO.setId(videochannel.getId());
        return videoChannelDTO;

    }

    @Transactional
    @Override
    public void joinVideoChannel(Long accountId, Long videoChannelId) throws VideoConferenceException {

        this.selectVideoChannel(videoChannelId);
        videoMemberRepository.save(VideoMember.builder().account(Account.builder().id(accountId).build()).videoChannel(VideoChannel.builder().id(videoChannelId).id(videoChannelId).build()).build());
        videoMemberRepository.flush();
        List<VideoMember> members = videoMemberRepository.selectAccountsByChannelId(videoChannelId);
        List<VideoMemberDTO> memberDTOList = members.stream().map(member -> {
            VideoMemberDTO memberDTO = modelMapper.map(member.getAccount(), VideoMemberDTO.class);
            memberDTO.setJoinDate(member.getCreatedAt());
            memberDTO.setId(member.getId());
            memberDTO.setUserId(member.getAccount().getId());
            return memberDTO;
        }).collect(Collectors.toList());

        messagingTemplate.convertAndSend("/sub/video/" + videoChannelId, new VideoConferenceMsgDTO(VideoConferenceControlMsg.GET_PARTICIPANTS_CONFIRMED, null, memberDTOList));
    }

    @Transactional
    @Override
    public void deleteVideoChannel(Long videoChannelId) throws VideoConferenceException {

        VideoChannel channel = videoChannelRepository.findById(videoChannelId).orElseThrow(() -> new VideoConferenceException(VideoConferenceErrorCode.CHANNEL_NOT_FOUND));
        channel.markDeleted();
        messagingTemplate.convertAndSend("/sub/video/" + videoChannelId, new VideoConferenceMsgDTO(VideoConferenceControlMsg.SHOULD_OUT_CHANNEL, null, null));

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
            memberDTO.setId(member.getId());
            memberDTO.setUserId(member.getAccount().getId());
            return memberDTO;
        }).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void deleteVideoChannelParticipant(Long memberId, Long channelId) throws VideoConferenceException {

        VideoMember member = videoMemberRepository.findById(memberId).orElseThrow(() -> new VideoConferenceException(VideoConferenceErrorCode.MEMBER_NOT_FOUND));
        videoMemberRepository.delete(member);
        videoMemberRepository.flush();
        List<VideoMember> members = videoMemberRepository.selectAccountsByChannelId(channelId);
        if (members.isEmpty()) {
            VideoChannel channel = videoChannelRepository.findById(channelId).orElseThrow(() -> new VideoConferenceException(VideoConferenceErrorCode.CHANNEL_NOT_FOUND));
            channel.markDeleted();
            return;
        }
        messagingTemplate.convertAndSendToUser(member.getAccount().getEmail(), "/sub/chat/" + channelId, new WebSocketChatDTO(null, null, "disconnect", "controll"));
        messagingTemplate.convertAndSendToUser(member.getAccount().getEmail(), "/sub/video/" + channelId, new VideoConferenceMsgDTO(VideoConferenceControlMsg.SHOULD_OUT_CHANNEL, null, null));

        List<VideoMemberDTO> memberDTOList = members.stream().map(m -> {
            VideoMemberDTO memberDTO = modelMapper.map(m.getAccount(), VideoMemberDTO.class);
            memberDTO.setJoinDate(m.getCreatedAt());
            memberDTO.setId(member.getId());
            memberDTO.setUserId(m.getAccount().getId());
            return memberDTO;
        }).collect(Collectors.toList());
        messagingTemplate.convertAndSend("/sub/video/" + channelId, new VideoConferenceMsgDTO(VideoConferenceControlMsg.GET_PARTICIPANTS_CONFIRMED, null, memberDTOList));

    }


    @Transactional(readOnly = true)
    @Override
    public boolean checkUserInVideoChannel(Long accountId, Long videoChannelId) {
        VideoMember member = videoMemberRepository.findByAccountIdAndVideoChannelId(accountId, videoChannelId);
        return member != null;
    }


    @Override
    public String joinVideoConferenceRoom(Long accountId, Long videoChannelId, String username, String userEmail) throws VideoConferenceException {

        if (!checkUserInVideoChannel(accountId, videoChannelId)) {
            throw new VideoConferenceException(VideoConferenceErrorCode.MEMBER_NOT_FOUND);
        }
        AccessToken token = new AccessToken(LIVEKIT_API_KEY, LIVEKIT_API_SECRET);
        String metadataJson = String.format("{\"userName\":\"%s\"}", username.replace("\"", "\\\"").replace("\n", "\\n"));

        token.setName(username);
        token.setMetadata(metadataJson);
        token.setIdentity(userEmail);
        token.setTtl(3600);
        token.addGrants(new RoomJoin(true), new RoomName(String.valueOf(videoChannelId)));
        return token.toJwt();

    }

    @Override
    public void handleVideoConferenceEvent(String userEmail, Long roomId, VideoConferenceControlMsg event) {

        if (VideoConferenceControlMsg.START_SCREEN_SHARING.equals(event)) {
            String currentSharingUserEmail = currentSharingUser.get(roomId);
            if (currentSharingUserEmail != null) {
                messagingTemplate.convertAndSendToUser(currentSharingUserEmail, "/sub/video/" + roomId, new VideoConferenceMsgDTO(VideoConferenceControlMsg.STOP_SCREEN_SHARING, null, null));
                sharingWaitingUser.put(roomId, userEmail);
            } else {
                messagingTemplate.convertAndSendToUser(userEmail, "/sub/video/" + roomId, new VideoConferenceMsgDTO(VideoConferenceControlMsg.START_SCREEN_SHARING_CONFIRMED, userEmail, null));
                currentSharingUser.put(roomId, userEmail);
            }
        } else if (VideoConferenceControlMsg.STOP_SCREEN_SHARING_CONFIRM.equals(event)) {
            currentSharingUser.remove(roomId);
            messagingTemplate.convertAndSend("/sub/video/" + roomId, new VideoConferenceMsgDTO(VideoConferenceControlMsg.STOP_SCREEN_SHARING_CONFIRMED, userEmail, null));
            if (sharingWaitingUser.get(roomId) != null) {
                currentSharingUser.put(roomId, sharingWaitingUser.get(roomId));
                messagingTemplate.convertAndSendToUser(currentSharingUser.get(roomId), "/sub/video/" + roomId, new VideoConferenceMsgDTO(VideoConferenceControlMsg.START_SCREEN_SHARING_CONFIRMED, currentSharingUser.get(roomId), null));
                sharingWaitingUser.remove(roomId);
            }
        }
    }

    @Override
    public void handleLiveKitHookEvent(String authHeader, String body) throws Exception {
        WebhookReceiver webhookReceiver = new WebhookReceiver(LIVEKIT_API_KEY, LIVEKIT_API_SECRET);

        LivekitWebhook.WebhookEvent event = webhookReceiver.receive(body, authHeader);

        log.info(event.toString());
    }

}
