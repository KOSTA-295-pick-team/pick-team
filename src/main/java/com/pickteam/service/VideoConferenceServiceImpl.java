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
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoConferenceServiceImpl implements VideoConferenceService {

    @Value("${LIVEKIT_API_KEY}")
    private String LIVEKIT_API_KEY;

    @Value("${LIVEKIT_API_SECRET}")
    private String LIVEKIT_API_SECRET;

    private final VideoChannelRepository videoChannelRepository;

    private final VideoMemberRepository videoMemberRepository;

    private ModelMapper modelMapper = new ModelMapper();

    private Map<Long, List<ParticipantDTO>> participantsList = new HashMap<>();

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private Map<Long, String> currentSharingUser = new HashMap<>();

    private Map<Long, String> sharingWaitingUser = new HashMap<>();

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
    public void deleteVideoChannelParticipant(Long memberId, Long channelId) throws VideoConferenceException {

        VideoMember member = videoMemberRepository.findById(memberId).orElseThrow(() -> new VideoConferenceException(VideoConferenceErrorCode.MEMBER_NOT_FOUND));
        videoMemberRepository.delete(member);
        messagingTemplate.convertAndSendToUser(member.getAccount().getEmail(), "/sub/chat/" + channelId, new WebSocketChatDTO(null, null, "disconnect", "controll"));
        messagingTemplate.convertAndSend("/sub/video/" + channelId, new VideoConferenceMsgDTO(VideoConferenceControlMsg.STOP_SCREEN_SHARING_CONFIRMED, member.getAccount().getEmail(), null));
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
        if (VideoConferenceControlMsg.GET_PARTICIPANTS.equals(event)) {
            messagingTemplate.convertAndSendToUser(userEmail, "/sub/video/" + roomId, new VideoConferenceMsgDTO(VideoConferenceControlMsg.GET_PARTICIPANTS_CONFIRMED, null, participantsList.get(roomId)));
        } else if (VideoConferenceControlMsg.START_SCREEN_SHARING.equals(event)) {
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
        if ("participant_joined".equals(event.getEvent()) || "participant_left".equals(event.getEvent())) {
            Long channelId = Long.parseLong(event.getRoom().getName());
            if ("participant_joined".equals(event.getEvent())) {
                ParticipantDTO participantDTO = new ParticipantDTO();
                participantDTO.setIdentity(event.getParticipant().getIdentity());
                participantDTO.setName(event.getParticipant().getName());
                participantDTO.setJoinedAt(new Date(event.getParticipant().getJoinedAt()));
                participantDTO.setMetaData(event.getParticipant().getMetadata());
                participantDTO.setState(event.getParticipant().getState().name());

                if (participantsList.containsKey(channelId)) {
                    participantsList.get(channelId).add(participantDTO);
                } else {
                    participantsList.put(channelId, new ArrayList<ParticipantDTO>());
                    participantsList.get(channelId).add(participantDTO);
                }
            } else {
                if (participantsList.containsKey(channelId)) {
                    participantsList.get(channelId).forEach(participantDTO -> {
                        if (participantDTO.getIdentity().equals(event.getParticipant().getIdentity())) {
                            participantsList.get(channelId).remove(participantDTO);
                        }
                    });
                }
            }
            messagingTemplate.convertAndSend("/sub/video/" + channelId, new VideoConferenceMsgDTO(VideoConferenceControlMsg.GET_PARTICIPANTS_CONFIRMED, null, participantsList.get(channelId)));
        }
    }
}
