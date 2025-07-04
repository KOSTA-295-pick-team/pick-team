// ChatRoomDetailResponse.java
package com.pickteam.dto.chat;

import com.pickteam.domain.chat.ChatRoom;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ChatRoomDetailResponse extends ChatRoomResponse {
    private List<ChatMemberResponse> members;
    private ChatMessageResponse lastMessage;
    
    public static ChatRoomDetailResponse from(ChatRoom chatRoom, boolean notificationEnabled) {
        ChatRoomDetailResponse response = new ChatRoomDetailResponse();
        // ... 기본 정보 설정
        response.members = chatRoom.getChatMembers().stream()
                .map(ChatMemberResponse::from)
                .collect(Collectors.toList());
        // ... lastMessage 설정
        return response;
    }
}