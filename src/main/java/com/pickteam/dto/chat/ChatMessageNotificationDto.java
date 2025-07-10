package com.pickteam.dto.chat;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatMessageNotificationDto {
    private Long messageId;
    private Long chatRoomId;
    private String content;
    private Long senderId;

    public static ChatMessageNotificationDto from(ChatMessageResponse message) {
        return ChatMessageNotificationDto.builder()
                .messageId(message.getId())
                .chatRoomId(message.getChatRoomId())
                .content(message.getContent())
                .senderId(message.getSenderId())
                .build();
    }
}