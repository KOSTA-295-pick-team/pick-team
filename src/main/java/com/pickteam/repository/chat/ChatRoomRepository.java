package com.pickteam.repository.chat;

import com.pickteam.domain.chat.ChatRoom;
import com.pickteam.domain.workspace.Workspace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 워크스페이스에서 삭제되지 않은 채팅방을 찾아 페이지 단위로 채팅방 정보를 리턴
 */
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Page<ChatRoom> findByWorkspaceAndIsDeletedFalse(Workspace workspace, Pageable pageable);
    ChatRoom findByIdAndIsDeletedFalse(Long chatRoomId);

}
