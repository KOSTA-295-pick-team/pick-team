package com.pickteam.dto.chat;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import com.pickteam.domain.chat.ChatMessage;

@Getter
@Builder
public class ChatMessageResponse {
    private final Long id;
    private final String content;
    private final Long senderId;
    private final Long chatRoomId;
    private final LocalDateTime sentAt;
    //private final boolean edited;

    public static ChatMessageResponse from(ChatMessage chatMessage) {
        return ChatMessageResponse.builder()
                .id(chatMessage.getId())
                .content(chatMessage.getContent())
                .senderId(chatMessage.getAccount().getId())
                .chatRoomId(chatMessage.getChatRoom().getId())
                .sentAt(chatMessage.getCreatedAt())
                //.edited(chatMessage.isEdited())
                .build();
    }
}