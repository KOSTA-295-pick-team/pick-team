package com.pickteam.config.websocket;

import com.pickteam.domain.videochat.VideoMember;
import com.pickteam.exception.VideoConferenceException;
import com.pickteam.exception.WebSocketChatErrorCode;
import com.pickteam.exception.WebSocketChatException;
import com.pickteam.repository.VideoMemberRepository;
import com.pickteam.security.UserPrincipal;
import com.pickteam.service.VideoConferenceService;
import com.pickteam.service.VideoConferenceServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Component
public class AuthChannelInterceptor implements ChannelInterceptor {


    private Map<String, Long> sessionChannelAccessCache = new ConcurrentHashMap<>();


    private final VideoConferenceService videoConferenceService;
    private final VideoMemberRepository videoMemberRepository;


    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                message, StompHeaderAccessor.class);

        if (StompCommand.SEND.equals(accessor.getCommand()) || StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {

            String sessionId = accessor.getSessionId();

            String[] dest = accessor.getDestination().split("/");
            String destChannelId = dest[dest.length - 1];
            if (!destChannelId.chars().allMatch(Character::isDigit)) {
                throw new WebSocketChatException(WebSocketChatErrorCode.INVALID_CHANNEL);
            }
            if (sessionChannelAccessCache.containsKey(sessionId)) {
                Long authorizedChannelId = sessionChannelAccessCache.get(sessionId);
                if (authorizedChannelId.equals(Long.parseLong(destChannelId))) {
                    return message;
                }
            }
            UserPrincipal userPrincipal = ((UserPrincipal) ((Authentication) accessor.getUser()).getPrincipal());

            VideoMember vm = videoMemberRepository.findByAccountIdAndVideoChannelId(userPrincipal.getId(), Long.parseLong(destChannelId));

            if (vm == null) {
                throw new WebSocketChatException(WebSocketChatErrorCode.CANNOT_ACCESS_CHANNEL);
            }
            Map<String, Object> attrs = accessor.getSessionAttributes();
            if (attrs != null) {

                attrs.put("videoMemberId", vm.getId());
                attrs.put("videoChannelId", Long.parseLong(destChannelId));
            }
            sessionChannelAccessCache.put(sessionId, Long.parseLong(destChannelId));
        }
        return message;
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) throws VideoConferenceException {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                event.getMessage(), StompHeaderAccessor.class);
        sessionChannelAccessCache.remove(accessor.getSessionId());
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        Long vmId = (Long)sessionAttributes.get("videoMemberId");
        Long vcId = (Long)sessionAttributes.get("videoChannelId");
        System.out.println("videoMemberId: " + vmId + ", videoChannelId: " + vcId);
        if(vmId != null && vcId != null) {
            videoConferenceService.deleteVideoChannelParticipant(vmId, vcId);
        }


    }

}
