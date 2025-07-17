// ChatRoomResponse.java
package com.pickteam.dto.chat;

import com.pickteam.domain.chat.ChatRoom;
import com.pickteam.domain.enums.ChatRoomType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatRoomResponse {
    private Long id;
    private String name;
    private ChatRoomType type;
    private Long workspaceId;
    private String workspaceName;
    private LocalDateTime createdAt;
    private int memberCount;

    public static ChatRoomResponse from(ChatRoom chatRoom) {
        ChatRoomResponse response = new ChatRoomResponse();
        response.id = chatRoom.getId();
        response.name = chatRoom.getName();
        response.type = chatRoom.getType();
        if (chatRoom.getWorkspace() != null) {
            response.workspaceId = chatRoom.getWorkspace().getId();
            response.workspaceName = chatRoom.getWorkspace().getName();
        }
        response.createdAt = chatRoom.getCreatedAt();
        response.memberCount = chatRoom.getChatMembers() != null
            ? chatRoom.getChatMembers().size()
            : 0;
        return response;
    }
}