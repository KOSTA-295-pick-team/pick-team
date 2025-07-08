package com.pickteam.service.chat;

import com.pickteam.domain.chat.ChatMember;
import com.pickteam.domain.chat.ChatMessage;
import com.pickteam.domain.chat.ChatRoom;
import com.pickteam.domain.user.Account;
import com.pickteam.dto.chat.ChatMessageListResponse;
import com.pickteam.dto.chat.ChatMessageRequest;
import com.pickteam.dto.chat.ChatMessageResponse;
import com.pickteam.repository.chat.ChatMessageRepository;
import com.pickteam.repository.chat.ChatRoomRepository;
import com.pickteam.repository.user.AccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final AccountRepository accountRepository;

    @Override
    public ChatMessageListResponse getMessagesAfter(Long chatRoomId, Long messageId) {
        ChatMessage baseMessage = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("기준 메시지를 찾을 수 없습니다."));

        List<ChatMessage> messages = chatMessageRepository
                .findByChatRoomIdAndCreatedAtAfterOrderByCreatedAtAsc(chatRoomId, baseMessage.getCreatedAt());

        return ChatMessageListResponse.from(messages);
    }

    @Override
    public ChatMessageListResponse getMessagesAfterTime(Long chatRoomId, LocalDateTime dateTime) {
        List<ChatMessage> messages = chatMessageRepository
                .findByChatRoomIdAndCreatedAtAfterOrderByCreatedAtAsc(chatRoomId, dateTime);

        return ChatMessageListResponse.from(messages);
    }

    @Override
    public ChatMessageListResponse getAllMessages(Long chatRoomId) {
        List<ChatMessage> messages = chatMessageRepository
                .findByChatRoomIdOrderByCreatedAtAsc(chatRoomId);

        return ChatMessageListResponse.from(messages);
    }

    @Override
    public ChatMessageListResponse getRecentMessages(Long chatRoomId, int limit) {
        Pageable pageable = PageRequest.ofSize(limit).withSort(Sort.by(Sort.Direction.DESC, "createdAt"));

        List<ChatMessage> messages = chatMessageRepository.findByChatRoomId(chatRoomId, pageable);

        // 최신순으로 가져온 후 → 다시 오름차순 정렬해서 반환
        messages.sort(Comparator.comparing(ChatMessage::getCreatedAt));

        return ChatMessageListResponse.from(messages);
    }

    @Override
    public Page<ChatMessageResponse> getMessagesByPage(Long chatRoomId, Long baseMessageId, Pageable pageable) {
        ChatMessage baseMessage = chatMessageRepository.findById(baseMessageId)
                .orElseThrow(() -> new EntityNotFoundException("기준 메시지가 존재하지 않습니다."));

        Page<ChatMessage> messages = chatMessageRepository
                .findByChatRoomIdAndCreatedAtLessThanEqual(chatRoomId, baseMessage.getCreatedAt(), pageable);

        return messages.map(ChatMessageResponse::from);
    }

    /**
     * 메시지 전송
     *
     * @param chatRoomId
     * @param request
     * @return
     */
    @Override
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
    public void deleteMessage(Long messageId, Long accountId) {

    }

    @Override
    public Page<ChatMessageResponse> searchMessagesByUser(Long chatRoomId, Long accountId, Pageable pageable) {
        return null;
    }

    @Override
    public List<ChatMessageResponse> searchAllMessagesByUser(Long chatRoomId, Long accountId, Pageable pageable) {
        return List.of();
    }

    @Override
    public Page<ChatMessageResponse> searchMessagesByContent(Long chatRoomId, String keyword, Pageable pageable) {
        return null;
    }

    @Override
    public List<ChatMessageResponse> searchAllMessagesByContent(Long chatRoomId, String keyword, Pageable pageable) {
        return List.of();
    }
}
