package com.pickteam.service.chat;

import com.pickteam.domain.chat.ChatMessage;
import com.pickteam.domain.chat.ChatRoom;
import com.pickteam.domain.user.Account;
import com.pickteam.domain.workspace.WorkspaceMember;
import com.pickteam.dto.chat.ChatMessageListResponse;
import com.pickteam.dto.chat.ChatMessageRequest;
import com.pickteam.dto.chat.ChatMessageResponse;
import com.pickteam.repository.chat.ChatMessageRepository;
import com.pickteam.repository.chat.ChatRoomRepository;
import com.pickteam.repository.user.AccountRepository;
import com.pickteam.repository.workspace.WorkspaceMemberRepository;
import com.pickteam.repository.workspace.WorkspaceRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final AccountRepository accountRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;


    @Override
    public ChatMessageListResponse getMessagesAfter(Long chatRoomId, Long messageId, Pageable pageable) {
        //채팅방 존재 여부 검증
        chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다."));

        ChatMessage baseMessage = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("기준 메시지를 찾을 수 없습니다."));
        Page<ChatMessage> messages = chatMessageRepository
                .findByChatRoomIdAndCreatedAtAfterOrderByCreatedAtAsc(
                        chatRoomId, baseMessage.getCreatedAt(), pageable);

        return ChatMessageListResponse.from(messages.map(ChatMessageResponse::from));
    }

    @Override
    public ChatMessageListResponse getMessagesAfterTime(Long chatRoomId, LocalDateTime dateTime, Pageable pageable) {
        //채팅방 존재 여부 검증
        chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다."));

        Page<ChatMessage> messages = chatMessageRepository
                .findByChatRoomIdAndCreatedAtAfterOrderByCreatedAtAsc(
                        chatRoomId, dateTime, pageable);

        return ChatMessageListResponse.from(messages.map(ChatMessageResponse::from));
    }

    @Override
    public ChatMessageListResponse getRecentMessages(Long chatRoomId, Pageable pageable) {
        //채팅방 존재 여부 검증
        chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다."));

        Page<ChatMessage> messages = chatMessageRepository
                .findByChatRoomIdOrderByCreatedAtDesc(chatRoomId, pageable);

        return ChatMessageListResponse.from(messages.map(ChatMessageResponse::from));
    }


    /**
     * 메시지 전송
     *
     * @param chatRoomId
     * @param request
     * @return
     */
    @Override
    @Transactional
    public ChatMessageResponse sendMessage(Long chatRoomId, ChatMessageRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다."));

        Account sender = accountRepository.findById(request.getSenderId())
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        boolean isMember = chatRoom.getChatMembers().stream()
                .anyMatch(cm -> cm.getAccount().getId().equals(sender.getId()));

        if (!isMember) {
            throw new IllegalArgumentException("이 채팅방에 속한 사용자가 아닙니다.");
        }

        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .account(sender)
                .content(request.getContent())
                .build();

        return ChatMessageResponse.from(chatMessageRepository.save(message));
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId, Long accountId, Long workspaceId, Long chatRoomId) {
        //작성자 혹은 워크스페이스 관리자만 메시지를 삭제할 수 있다
        //accountId가 워크스페이스 관리자인지 검사한다.
        //accountId와 chatMessage의 작성자를 비교해 작성자가 아니면 예외를 던진다
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다."));

        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("메시지를 찾을 수 없습니다."));

        if (!message.getChatRoom().getId().equals(chatRoomId)) {
            throw new IllegalArgumentException("해당 채팅방에 속한 메시지가 아닙니다.");
        }

        //요청자에게 삭제 권한이 있는지 검사 (작성자 혹은 워크스페이스 관리자)
        List<WorkspaceMember> members = workspaceMemberRepository
                .findByAccountIdAndStatus(accountId, WorkspaceMember.MemberStatus.ACTIVE);

        //레포지토리에서 리스트 형태로 결과를 주므로 결과물을 리스트로 받은 뒤 필터한다
        WorkspaceMember targetMember = members.stream()
                .filter(m -> m.getWorkspace().getId().equals(workspaceId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("해당 워크스페이스의 멤버가 아닙니다."));

        //작성자인지 먼저 검사
        if (!message.getAccount().getId().equals(accountId)
                && targetMember.getRole() != WorkspaceMember.MemberRole.ADMIN) {
            throw new IllegalArgumentException("메시지 삭제 권한이 없습니다.");
        }
        //여기까지 내려왔으면 메시지를 삭제한다.
        message.markDeleted();//soft-delete 처리
        chatMessageRepository.save(message);
    }

    @Override
    public Page<ChatMessageResponse> searchMessagesByUser(Long chatRoomId, Long accountId, Pageable pageable) {
        return null;
    }


    @Override
    public Page<ChatMessageResponse> searchMessagesByContent(Long chatRoomId, String keyword, Pageable pageable) {
        return null;
    }

}