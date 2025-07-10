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
     * 워크스페이스에 속한 채팅방 목록을 조회합니다.
     * 삭제되지 않은(active) 채팅방만 반환됩니다.
     *
     * @param workspaceId 워크스페이스 ID
     * @param pageable 페이징 및 정렬 정보
     * @return 채팅방 목록과 페이징 정보
     */
    Page<ChatRoomResponse> getChatRoomsByWorkspace(Long workspaceId, Pageable pageable);

    /**
     * 새로운 채팅방을 생성합니다.
     * 생성자는 해당 워크스페이스의 멤버여야 합니다.
     *
     * @param creatorId 생성자 ID
     * @param request 채팅방 생성 정보 (워크스페이스 ID, 채팅방 이름, 멤버 목록 등)
     * @return 생성된 채팅방 정보
     */
    ChatRoomResponse createChatRoom(Long creatorId, ChatRoomCreateRequest request);

    /**
     * 채팅방의 제목을 수정합니다.
     * 채팅방 멤버만 제목을 수정할 수 있습니다.
     *
     * @param creatorId 수정 요청자 ID
     * @param request 채팅방 제목 변경 정보
     * @param workspaceId 워크스페이스 ID
     * @param chatId 채팅방 ID
     * @return 수정된 채팅방 정보
     */
    ChatRoomResponse updateChatRoomTitle(Long creatorId, ChatRoomUpdateTitleRequest request, Long workspaceId, Long chatId);

    /**
     * 1:1 채팅방을 생성합니다.
     * 일반 채팅방 생성과 동일한 프로세스를 따릅니다.
     *
     * @param creatorId 생성자 ID
     * @param request 채팅방 생성 정보
     * @return 생성된 DM 채팅방 정보
     */
    ChatRoomResponse createDmChatRoom(Long creatorId, ChatRoomCreateRequest request);

    /**
     * 채팅방을 삭제 처리합니다 (soft delete).
     * 모든 멤버가 나간 경우 자동으로 호출됩니다.
     *
     * @param chatRoomId 채팅방 ID
     * @param workspaceId 워크스페이스 ID
     */
    void deleteChatRoom(Long chatRoomId, Long workspaceId);

    /**
     * 채팅방의 상세 정보를 조회합니다.
     * (Work in Progress)
     *
     * @param chatRoomId 채팅방 ID
     * @return 채팅방 상세 정보 (멤버 목록, 채팅 내역 등)
     */
    ChatRoomDetailResponse getChatRoomDetails(Long chatRoomId);

    /**
     * 채팅방 알림을 활성화합니다.
     * (Work in Progress)
     *
     * @param chatRoomId 채팅방 ID
     * @param accountId 사용자 ID
     */
    void enableChatRoomNotification(Long chatRoomId, Long accountId);

    /**
     * 채팅방 알림을 비활성화합니다.
     * (Work in Progress)
     *
     * @param chatRoomId 채팅방 ID
     * @param accountId 사용자 ID
     */
    void disableChatRoomNotification(Long chatRoomId, Long accountId);
}