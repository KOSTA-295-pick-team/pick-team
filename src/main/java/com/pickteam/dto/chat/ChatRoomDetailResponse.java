// ChatRoomDetailResponse.java
package com.pickteam.dto.chat;

import com.pickteam.domain.chat.ChatMessage;
import com.pickteam.domain.chat.ChatRoom;
import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ChatRoomDetailResponse extends ChatRoomResponse {
    private List<ChatMemberResponse> members;
    private List<ChatMessageResponse> messages;
    private ChatMessageResponse lastMessage;


    public static ChatRoomDetailResponse from(ChatRoom chatRoom, boolean notificationEnabled) {
        ChatRoomDetailResponse response = new ChatRoomDetailResponse();

        // 상위 클래스 필드 채우기
        response.setId(chatRoom.getId());
        response.setName(chatRoom.getName());
        response.setType(chatRoom.getType());
        if (chatRoom.getWorkspace() != null) {
            response.setWorkspaceId(chatRoom.getWorkspace().getId());
            response.setWorkspaceName(chatRoom.getWorkspace().getName());
        }
        response.setCreatedAt(chatRoom.getCreatedAt());
        response.setMemberCount(chatRoom.getChatMembers() != null ? chatRoom.getChatMembers().size() : 0);

        // 멤버 리스트 매핑
        response.setMembers(chatRoom.getChatMembers().stream()
                .map(ChatMemberResponse::from)
                .collect(Collectors.toList()));

        // 메시지 리스트 매핑
        List<ChatMessageResponse> messageResponses = chatRoom.getChatMessages().stream()
                .map(ChatMessageResponse::from)
                .collect(Collectors.toList());
        response.setMessages(messageResponses);

        // 가장 최근 메시지 추출
        chatRoom.getChatMessages().stream()
                .max(Comparator.comparing(ChatMessage::getCreatedAt))
                .map(ChatMessageResponse::from)
                .ifPresent(response::setLastMessage);

        return response;
    }
}