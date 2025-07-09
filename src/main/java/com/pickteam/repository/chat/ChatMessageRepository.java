package com.pickteam.repository.chat;

import com.pickteam.domain.chat.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId, Pageable pageable);
    
    Page<ChatMessage> findByChatRoomIdAndCreatedAtAfterOrderByCreatedAtAsc(
            Long chatRoomId, LocalDateTime createdAt, Pageable pageable);
    
    Page<ChatMessage> findByChatRoomIdAndCreatedAtLessThanEqualOrderByCreatedAtDesc(
            Long chatRoomId, LocalDateTime createdAt, Pageable pageable);

    Page<ChatMessage> findByChatRoomIdAndCreatedAtLessThanEqual(Long chatRoomId, LocalDateTime createdAt, Pageable pageable);

    Optional<ChatMessage> findByIdAndChatRoomIdAndIsDeletedFalse(Long messageId, Long chatRoomId);
}