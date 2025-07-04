package com.pickteam.service.chat;

import com.pickteam.domain.chat.ChatMessage;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

public interface ChatMessageService {
    /**
     * 특정 메시지 ID 이후의 채팅 목록을 가져옵니다.
     * @param chatRoomId 채팅방 ID
     * @param messageId 기준이 되는 메시지 ID
     * @return 해당 메시지 이후의 채팅 목록
     */
    List<ChatMessage> getMessagesAfter(Long chatRoomId, Long messageId);

    /**
     * 특정 시간 이후의 채팅 목록을 가져옵니다.
     * @param chatRoomId 채팅방 ID
     * @param dateTime 기준 시간
     * @return 해당 시간 이후의 채팅 목록
     */
    List<ChatMessage> getMessagesAfterTime(Long chatRoomId, LocalDateTime dateTime);

    /**
     * 최근 채팅 메시지를 지정된 개수만큼 가져옵니다.
     * @param chatRoomId 채팅방 ID
     * @param limit 가져올 메시지 개수
     * @return 최근 채팅 메시지 목록
     */
    List<ChatMessage> getRecentMessages(Long chatRoomId, int limit);

    /**
     * 특정 메시지 ID를 기준으로 페이징 처리된 채팅을 가져옵니다.
     * @param chatRoomId 채팅방 ID
     * @param baseMessageId 기준 메시지 ID
     * @param pageable 페이징 정보
     * @return 페이징 처리된 채팅 메시지
     */
    Page<ChatMessage> getMessagesByPage(Long chatRoomId, Long baseMessageId, Pageable pageable);

    /**
     * 새로운 메시지를 전송합니다.
     * @param chatRoomId 채팅방 ID
     * @param senderId 발신자 ID
     * @param content 메시지 내용
     * @return 생성된 채팅 메시지
     */
    ChatMessage sendMessage(Long chatRoomId, Long senderId, String content);

    /**
     * 메시지를 삭제합니다.
     * @param messageId 삭제할 메시지 ID
     * @param accountId 삭제 요청자 ID
     */
    void deleteMessage(Long messageId, Long accountId);

    /**
     * 특정 사용자가 보낸 메시지를 검색합니다.
     * @param chatRoomId 채팅방 ID
     * @param accountId 검색할 사용자 ID
     * @param pageable 페이징 정보
     * @return 검색된 메시지 목록
     */
    Page<ChatMessage> searchMessagesByUser(Long chatRoomId, Long accountId, Pageable pageable);

    /**
     * 메시지 내용으로 검색합니다.
     * @param chatRoomId 채팅방 ID
     * @param keyword 검색할 키워드
     * @param pageable 페이징 정보
     * @return 검색된 메시지 목록
     */
    Page<ChatMessage> searchMessagesByContent(Long chatRoomId, String keyword, Pageable pageable);
}