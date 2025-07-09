package com.pickteam.controller;


import com.pickteam.dto.WebSocketChatDTO;
import com.pickteam.exception.WebSocketChatErrorCode;
import com.pickteam.exception.WebSocketChatException;
import com.pickteam.security.UserPrincipal;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class WebSocketChatController {

    @MessageMapping("/chat/{roomId}")
    @SendTo("/sub/chat/{roomId}")
    public WebSocketChatDTO send(WebSocketChatDTO msg, Principal principal) {

        UserPrincipal principalUser = (UserPrincipal)((UsernamePasswordAuthenticationToken) principal).getPrincipal();

        msg.setType("chat");
        msg.setSenderEmail(principalUser.getEmail());
        msg.setSenderName(principalUser.getName());
        return msg;
    }
}
