package com.pickteam.config.websocket;

import com.pickteam.domain.videochat.VideoMember;
import com.pickteam.exception.WebSocketChatErrorCode;
import com.pickteam.exception.WebSocketChatException;
import com.pickteam.repository.VideoMemberRepository;
import com.pickteam.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
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


@Component
public class AuthChannelInterceptor implements ChannelInterceptor {


    private Map<String,Long> sessionChannelAccessCache = new ConcurrentHashMap<>();

    @Autowired
    private VideoMemberRepository videoMemberRepository;

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
                if(authorizedChannelId.equals(Long.parseLong(destChannelId))) {
                    return message;
                }
            }
            UserPrincipal userPrincipal = ((UserPrincipal) ((Authentication) accessor.getUser()).getPrincipal());

            VideoMember vm = videoMemberRepository.findByAccountIdAndVideoChannelId(userPrincipal.getId(), Long.parseLong(destChannelId));

            if (vm == null) {
                throw new WebSocketChatException(WebSocketChatErrorCode.CANNOT_ACCESS_CHANNEL);
            }

            sessionChannelAccessCache.put(sessionId,Long.parseLong(destChannelId));

        }
        return message;
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        String sessionId = MessageHeaderAccessor.getAccessor(
                event.getMessage(), StompHeaderAccessor.class).getSessionId();
        sessionChannelAccessCache.remove(sessionId);
    }

}

