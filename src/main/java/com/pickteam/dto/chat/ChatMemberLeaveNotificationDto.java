package com.pickteam.dto.chat;

import com.pickteam.domain.user.Account;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 채팅방 멤버 퇴장 알림을 위한 DTO
 * SSE를 통해 채팅방의 다른 멤버들에게 멤버의 퇴장을 알립니다.
 */
@Getter
@Builder
public class ChatMemberLeaveNotificationDto {
    /** 채팅방 ID */
    private Long chatRoomId;
    
    /** 퇴장한 멤버의 ID */
    private Long leftMemberId;
    
    /** 퇴장한 멤버의 이름 */
    private String leftMemberName;
    
    /** 퇴장 시간 */
    private LocalDateTime timestamp;

    /**
     * Account 엔티티로부터 알림 DTO를 생성합니다.
     * 
     * @param chatRoomId 채팅방 ID
     * @param leftAccount 퇴장한 사용자 계정 정보
     * @return 생성된 알림 DTO
     */
    public static ChatMemberLeaveNotificationDto from(Long chatRoomId, Account leftAccount) {
        return ChatMemberLeaveNotificationDto.builder()
                .chatRoomId(chatRoomId)
                .leftMemberId(leftAccount.getId())
                .leftMemberName(leftAccount.getName())
                .timestamp(LocalDateTime.now())
                .build();
    }
}