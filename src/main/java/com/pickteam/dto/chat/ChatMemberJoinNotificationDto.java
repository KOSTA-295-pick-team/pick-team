package com.pickteam.dto.chat;

import com.pickteam.domain.user.Account;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 채팅방 멤버 입장 알림을 위한 DTO
 * SSE를 통해 채팅방의 다른 멤버들에게 새로운 멤버의 입장을 알립니다.
 */
@Getter
@Builder
public class ChatMemberJoinNotificationDto {
    /** 채팅방 ID */
    private Long chatRoomId;
    
    /** 입장한 멤버의 ID */
    private Long joinedMemberId;
    
    /** 입장한 멤버의 이름 */
    private String joinedMemberName;
    
    /** 입장 시간 */
    private LocalDateTime timestamp;

    /**
     * Account 엔티티로부터 알림 DTO를 생성합니다.
     * 
     * @param chatRoomId 채팅방 ID
     * @param joinedAccount 입장한 사용자 계정 정보
     * @return 생성된 알림 DTO
     */
    public static ChatMemberJoinNotificationDto from(Long chatRoomId, Account joinedAccount) {
        return ChatMemberJoinNotificationDto.builder()
                .chatRoomId(chatRoomId)
                .joinedMemberId(joinedAccount.getId())
                .joinedMemberName(joinedAccount.getName())
                .timestamp(LocalDateTime.now())
                .build();
    }
}