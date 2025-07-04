package com.pickteam.repository.chat;

import com.pickteam.domain.chat.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {

}
