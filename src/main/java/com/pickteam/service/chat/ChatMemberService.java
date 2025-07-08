package com.pickteam.service.chat;

import com.pickteam.domain.chat.ChatMember;
import com.pickteam.domain.chat.ChatRoom;
import com.pickteam.dto.chat.ChatMemberResponse;
import jakarta.transaction.Transactional;

import java.util.List;

public interface ChatMemberService {
    /**
     * 특정 채팅방에 사용자를 입장시킵니다.
     * @param accountId 입장할 사용자 ID
     * @return 생성된 ChatMember 객체
     */
    ChatMember joinChatRoom(Long accountId, Long workspaceId, Long chatId);

    /**
     * 특정 채팅방에서 사용자를 나가게 합니다.
     * @param chatRoomId 채팅방 ID
     * @param accountId 나갈 사용자 ID
     */
    void leaveChatRoom(Long workspaceId, Long chatRoomId, Long accountId);

    /**
     * 특정 채팅방의 멤버 상세정보를 조회합니다.
     *
     * @param chatRoomId 채팅방 ID
     * @param accountId  조회할 멤버 ID
     * @return ChatMember 상세 정보
     */
    List<ChatMemberResponse> getChatMemberDetail(Long chatRoomId, Long accountId);

    /**
     * 특정 채팅방에서 멤버를 강제로 내보냅니다.
     * @param chatRoomId 채팅방 ID
     * @param targetAccountId 내보낼 멤버 ID
     * @param operatorAccountId 작업을 수행하는 운영자 ID
     */
    void kickChatMember(Long chatRoomId, Long targetAccountId, Long operatorAccountId);

    /**
     * 특정 채팅방에서 마지막으로 읽은 메시지를 갱신합니다.
     * @param chatRoomId 채팅방 ID
     * @param accountId 사용자 ID
     * @param messageId 마지막으로 읽은 메시지 ID
     */
    void updateLastReadMessage(Long workspaceId, Long chatRoomId, Long accountId, Long messageId);

    /**
     * 특정 사용자가 참여 중인 모든 채팅방 목록을 조회합니다.
     * @param accountId 사용자 ID
     * @return 사용자가 참여 중인 채팅방 목록
     */
    List<ChatRoom> getChatRoomsByMember(Long accountId);

    /**
     * 특정 채팅방의 모든 참여 멤버 목록을 조회합니다.
     * @param chatRoomId 채팅방 ID
     * @return 채팅방 참여 멤버 목록
     */
    List<ChatMember> getChatMembers(Long chatRoomId);
}