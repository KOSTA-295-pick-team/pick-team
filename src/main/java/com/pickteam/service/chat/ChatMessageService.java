package com.pickteam.service.chat;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pickteam.dto.chat.ChatMessageRequest;
import com.pickteam.dto.chat.ChatMessageResponse;
import com.pickteam.dto.chat.ChatMessageListResponse;

public interface ChatMessageService {

    /**
     * 특정 메시지 이후의 메시지를 조회
     * @param chatRoomId
     * @param messageId
     * @return
     */

    ChatMessageListResponse getMessagesAfter(Long chatRoomId, Long messageId, Pageable pageable);
    /**
     * 특정 시간 이후의 메시지를 조회
     * @param chatRoomId
     * @param dateTime
     * @return
     */
    ChatMessageListResponse getMessagesAfterTime(Long chatRoomId, LocalDateTime dateTime, Pageable pageable);

    /**
     * 특정 채팅방의 가장 최근 메시지로부터 n개의 메시지 목록을 페이징해서 조회
     * @param chatRoomId
     * @return
     */
    ChatMessageListResponse getRecentMessages(Long chatRoomId, Pageable pageable);

    /**
     * 특정 사용자가 보낸 메시지를 조회
     * @param chatRoomId
     * @param accountId
     * @param pageable
     * @return
     */
    Page<ChatMessageResponse> searchMessagesByUser(Long chatRoomId, Long accountId, Pageable pageable);


    /**
     * 특정 키워드를 포함한 메시지 검색
     * @param chatRoomId
     * @param keyword
     * @param pageable
     * @return
     */
    Page<ChatMessageResponse> searchMessagesByContent(Long chatRoomId, String keyword, Pageable pageable);

    /**
     * 메시지를 전송
     * @param chatRoomId
     * @param request
     * @return
     */

    ChatMessageResponse sendMessage(Long chatRoomId, ChatMessageRequest request);
    /**
     * 메시지를 삭제
     * @param messageId
     * @param accountId
     */
    void deleteMessage(Long messageId, Long accountId, Long WorkspaceId, Long chatId);

}