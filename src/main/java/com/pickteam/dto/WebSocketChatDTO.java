package com.pickteam.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class WebSocketChatDTO {
    private String senderEmail;
    private String senderName;
    private String message;
    private String type;
}
