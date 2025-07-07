package com.pickteam.service.chat;

import com.pickteam.dto.chat.ChatRoomCreateRequest;
import com.pickteam.dto.chat.ChatRoomDetailResponse;
import com.pickteam.dto.chat.ChatRoomResponse;
import com.pickteam.dto.chat.ChatRoomUpdateTitleRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

public interface ChatRoomService {
    /**
     * 특정 워크스페이스에 속한 채팅방 목록을 조회합니다.
     */
    Page<ChatRoomResponse> getChatRoomsByWorkspace(Long workspaceId, Pageable pageable);

    /**
     * 새로운 채팅방을 생성합니다.
     */
    public ChatRoomResponse createChatRoom(Long creatorId, ChatRoomCreateRequest request);

    /**
     * 채팅방 제목을 수정합니다.
     */
    ChatRoomResponse updateChatRoomTitle(Long creatorId, ChatRoomUpdateTitleRequest request);

    /**
     * 1:1 채팅방을 생성합니다.
     */
    ChatRoomResponse createDmChatRoom(Long creatorId, ChatRoomCreateRequest request);

    /**
     * 채팅방을 삭제합니다.
     */
    void deleteChatRoom(Long chatRoomId, Long accountId);

    /**
     * ID로 채팅방 상세 정보를 조회합니다.
     */
    ChatRoomDetailResponse getChatRoomDetails(Long chatRoomId);

    /**
     * 채팅방 알림을 활성화합니다.
     */
    void enableChatRoomNotification(Long chatRoomId, Long accountId);

    /**
     * 채팅방 알림을 비활성화합니다.
     */
    void disableChatRoomNotification(Long chatRoomId, Long accountId);
}