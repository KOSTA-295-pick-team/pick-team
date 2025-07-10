package com.pickteam.service.chat;

import com.pickteam.domain.chat.ChatMember;
import com.pickteam.domain.chat.ChatRoom;
import com.pickteam.dto.chat.ChatMemberResponse;
import jakarta.transaction.Transactional;

import java.util.List;

public interface ChatMemberService {
    /**
     * 채팅방에 새로운 멤버를 추가합니다.
     * 입장 시 다른 멤버들에게 알림이 전송됩니다.
     *
     * @param accountId 입장할 사용자의 ID
     * @param workspaceId 워크스페이스 ID
     * @param chatId 채팅방 ID
     * @return 생성된 채팅방 멤버 정보
     */
    ChatMember joinChatRoom(Long accountId, Long workspaceId, Long chatId);

    /**
     * 채팅방에서 멤버를 제거합니다 (soft delete).
     * 퇴장 시 다른 멤버들에게 알림이 전송됩니다.
     *
     * @param workspaceId 워크스페이스 ID
     * @param chatRoomId 채팅방 ID
     * @param accountId 퇴장할 사용자의 ID
     */
    void leaveChatRoom(Long workspaceId, Long chatRoomId, Long accountId);

    /**
     * 채팅방의 모든 멤버 상세정보를 조회합니다.
     *
     * @param chatRoomId 채팅방 ID
     * @param accountId 조회 요청자 ID
     * @return 채팅방 멤버 상세정보 목록
     */
    List<ChatMemberResponse> getChatMemberDetail(Long chatRoomId, Long accountId);

    /**
     * 채팅방에서 특정 멤버를 강제 퇴장시킵니다.
     * (Deprecated - 제거 예정)
     *
     * @param chatRoomId 채팅방 ID
     * @param targetAccountId 퇴장시킬 멤버의 ID
     * @param operatorAccountId 작업을 수행하는 관리자의 ID
     */
    void kickChatMember(Long chatRoomId, Long targetAccountId, Long operatorAccountId);

    /**
     * 채팅방에서 멤버의 마지막 읽은 메시지를 갱신합니다.
     *
     * @param workspaceId 워크스페이스 ID
     * @param chatRoomId 채팅방 ID
     * @param accountId 사용자 ID
     * @param messageId 마지막으로 읽은 메시지 ID
     */
    void updateLastReadMessage(Long workspaceId, Long chatRoomId, Long accountId, Long messageId);

    /**
     * 특정 사용자가 참여 중인 모든 채팅방을 조회합니다.
     * 삭제되지 않은(active) 채팅방만 반환됩니다.
     *
     * @param accountId 사용자 ID
     * @return 사용자가 참여 중인 채팅방 목록
     */
    List<ChatRoom> getChatRoomsByMember(Long accountId);

    /**
     * 채팅방의 현재 활성 멤버 목록을 조회합니다.
     * 삭제되지 않은(active) 멤버만 반환됩니다.
     *
     * @param chatRoomId 채팅방 ID
     * @return 채팅방의 활성 멤버 목록
     */
    List<ChatMember> getChatMembers(Long chatRoomId);
}