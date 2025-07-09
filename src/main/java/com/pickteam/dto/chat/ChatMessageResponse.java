// ChatMessageResponse.java
package com.pickteam.dto.chat;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import com.pickteam.domain.chat.ChatMessage;

@Getter
@Setter
public class ChatMessageResponse {
    private Long id;
    private String content;
    private Long senderId;
    private LocalDateTime sentAt;
    //private boolean edited;

    public static ChatMessageResponse from(ChatMessage chatMessage) {
        ChatMessageResponse response = new ChatMessageResponse();
        response.id = chatMessage.getId();
        response.content = chatMessage.getContent();
        response.senderId = chatMessage.getAccount().getId();//Account의 Id 정보 (Entity를 직접 노출시키지 않는다)
        response.sentAt = chatMessage.getCreatedAt();
//        response.edited = chatMessage.isEdited(); //수정여부 (아직 entity에 필드 없음)
        return response;
    }
}