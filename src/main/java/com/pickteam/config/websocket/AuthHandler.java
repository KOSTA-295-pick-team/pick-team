package com.pickteam.config.websocket;

import com.pickteam.exception.WebSocketChatErrorCode;
import com.pickteam.exception.WebSocketChatException;
import com.pickteam.security.UserPrincipal;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Component
public class AuthHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {


        Authentication authentication = (Authentication) attributes.get("authentication");

        if (authentication == null || !authentication.isAuthenticated() || authentication.getClass() == AnonymousAuthenticationToken.class) {
            throw new WebSocketChatException(WebSocketChatErrorCode.INVALID_USER);
        }

        return authentication; // 이 Principal이 이후 STOMP 메시지 처리에서 사용됨
    }
}