package com.pickteam.repository.chat;

import com.pickteam.domain.chat.ChatAttach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatAttachRepository extends JpaRepository<ChatAttach, Long> {

}
