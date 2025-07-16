package com.pickteam.controller;


import com.pickteam.dto.WebSocketChatDTO;
import com.pickteam.security.UserPrincipal;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class WebSocketChatController {
    private Map<Long, List<WebSocketChatDTO>> chatLogs = new ConcurrentHashMap<>();

    @MessageMapping("/chat/{roomId}")
    @SendTo("/sub/chat/{roomId}")
    public WebSocketChatDTO send(WebSocketChatDTO msg, Principal principal,@DestinationVariable Long roomId) {
        if ("init".equals(msg.getType())) {
            System.out.println("여기 오니??");
            WebSocketChatDTO chat = new WebSocketChatDTO();
            chat.setLogs(chatLogs.get(roomId) == null ? new ArrayList<>() : chatLogs.get(roomId));
            chat.setType("init");
            return chat;
        }
        UserPrincipal principalUser = (UserPrincipal) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();

        msg.setType("chat");
        msg.setSenderEmail(principalUser.getEmail());
        msg.setSenderName(principalUser.getName());

        if (!chatLogs.containsKey(roomId)) {
            chatLogs.put(roomId, new ArrayList<>());
        }
        chatLogs.get(roomId).add(msg);
        return msg;
    }
}
