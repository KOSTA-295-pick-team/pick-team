package com.pickteam.service.chat;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;

import com.pickteam.dto.chat.ChatMessageRequest;
import com.pickteam.dto.chat.ChatMessageResponse;
import com.pickteam.dto.chat.ChatMessageListResponse;

public interface ChatMessageService {

/**
 * 특정 메시지 이후의 메시지들을 조회합니다.
 *
 * @param chatRoomId 조회할 채팅방의 ID
 * @param messageId 기준이 되는 메시지 ID
 * @param pageable 페이징 정보
 * @return 메시지 목록과 페이징 정보가 포함된 응답
 */
ChatMessageListResponse getMessagesAfter(Long chatRoomId, Long messageId, Pageable pageable);

/**
 * 특정 시간 이후의 메시지들을 조회합니다.
 *
 * @param chatRoomId 조회할 채팅방의 ID
 * @param dateTime 기준이 되는 시간
 * @param pageable 페이징 정보
 * @return 메시지 목록과 페이징 정보가 포함된 응답
 */
ChatMessageListResponse getMessagesAfterTime(Long chatRoomId, LocalDateTime dateTime, Pageable pageable);

/**
 * 채팅방의 최근 메시지들을 조회합니다.
 *
 * @param chatRoomId 조회할 채팅방의 ID
 * @param pageable 페이징 정보 (정렬: 생성일시 내림차순)
 * @return 메시지 목록과 페이징 정보가 포함된 응답
 */
ChatMessageListResponse getRecentMessages(Long chatRoomId, Pageable pageable);

/**
 * 새로운 메시지를 전송합니다.
 *
 * @param chatRoomId 메시지를 전송할 채팅방의 ID
 * @param request 전송할 메시지 정보 (내용, 발신자 ID 등)
 * @return 저장된 메시지 정보
 */
ChatMessageResponse sendMessage(Long chatRoomId, ChatMessageRequest request);

/**
 * 메시지를 삭제 처리합니다 (soft delete).
 * 작성자 본인이나 워크스페이스 관리자만 삭제할 수 있습니다.
 *
 * @param messageId 삭제할 메시지의 ID
 * @param accountId 삭제를 요청한 사용자의 ID
 * @param workspaceId 워크스페이스 ID
 * @param chatRoomId 채팅방 ID
 */
void deleteMessage(Long messageId, Long accountId, Long workspaceId, Long chatRoomId);

// ------------------------------------- DEPRECATED --------------------------------------------------------------
//    TODO : 채팅방 검색 기능이 필요할 경우 구현
//    /**
//     * 특정 사용자가 보낸 메시지를 조회
//     * @param chatRoomId
//     * @param accountId
//     * @param pageable
//     * @return
//     */
//    Page<ChatMessageResponse> searchMessagesByUser(Long chatRoomId, Long accountId, Pageable pageable);
//
//
//    /**
//     * 특정 키워드를 포함한 메시지 검색
//     * @param chatRoomId
//     * @param keyword
//     * @param pageable
//     * @return
//     */
//    Page<ChatMessageResponse> searchMessagesByContent(Long chatRoomId, String keyword, Pageable pageable);


}