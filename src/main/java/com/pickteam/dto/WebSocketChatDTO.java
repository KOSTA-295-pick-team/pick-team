package com.pickteam.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class WebSocketChatDTO {
    private String senderEmail;
    private String senderName;
    private String message;
    private String type;
    public WebSocketChatDTO(String senderEmail, String senderName, String message, String type) {
        this.senderEmail = senderEmail;
        this.senderName = senderName;
        this.message = message;
        this.type = type;
    }
    private List<WebSocketChatDTO> logs = new ArrayList<>();
}
