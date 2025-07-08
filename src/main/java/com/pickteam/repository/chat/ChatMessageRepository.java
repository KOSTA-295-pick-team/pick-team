package com.pickteam.repository.chat;

import com.pickteam.domain.chat.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByChatRoomId(Long chatRoomId, Pageable pageable);

    List<ChatMessage> findByChatRoomIdAndCreatedAtAfterOrderByCreatedAtAsc(Long chatRoomId, LocalDateTime createdAt);

    Page<ChatMessage> findByChatRoomIdAndCreatedAtLessThanEqual(Long chatRoomId, LocalDateTime createdAt, Pageable pageable);

    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);
}
