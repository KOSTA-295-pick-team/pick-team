package com.pickteam.repository.chat;

import com.pickteam.domain.chat.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {
    Optional<ChatMember> findByChatRoomIdAndAccountId(Long chatRoomId, Long accountId);
    
    Optional<ChatMember> findByChatRoomIdAndAccountIdAndIsDeletedFalse(Long chatRoomId, Long accountId);

    List<ChatMember> findAllByChatRoomIdAndIsDeletedFalse(Long chatRoomId);

    List<ChatMember> findAllByAccountIdAndIsDeletedFalse(Long accountId);
}