// ChatMemberResponse.java
package com.pickteam.dto.chat;

import com.pickteam.domain.chat.ChatMember;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMemberResponse {
    private Long id;
    private Long account;
    private String name;
    private String profileImageUrl;
    private LocalDateTime joinedAt;
    //private boolean notificationEnabled;

    public static ChatMemberResponse from(ChatMember chatMember) {
        ChatMemberResponse response = new ChatMemberResponse();
        response.id = chatMember.getId();
        response.account = chatMember.getAccount().getId();
        response.name = chatMember.getAccount().getName();
        response.profileImageUrl = chatMember.getAccount().getProfileImageUrl();
        response.joinedAt = chatMember.getCreatedAt();
        //response.notificationEnabled = chatMember.isNotificationEnabled();
        return response;
    }
}