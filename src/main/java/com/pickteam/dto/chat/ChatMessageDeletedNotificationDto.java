package com.pickteam.dto.chat;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatMessageDeletedNotificationDto {
    private Long messageId;
    private Long chatRoomId;

    public static ChatMessageDeletedNotificationDto of(Long chatRoomId, Long messageId) {
        return ChatMessageDeletedNotificationDto.builder()
                .messageId(messageId)
                .chatRoomId(chatRoomId)
                .build();
    }
}