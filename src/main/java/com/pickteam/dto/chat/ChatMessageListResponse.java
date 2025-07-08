package com.pickteam.dto.chat;

import com.pickteam.domain.chat.ChatMessage;
import lombok.Data;

import java.util.List;

@Data
public class ChatMessageListResponse {
    private List<ChatMessageResponse> messages;

    public static ChatMessageListResponse from(List<ChatMessage> messageList) {
        ChatMessageListResponse response = new ChatMessageListResponse();
        response.setMessages(messageList.stream()
                .map(ChatMessageResponse::from)
                .toList());
        return response;
    }
}