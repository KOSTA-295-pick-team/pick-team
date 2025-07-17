package com.pickteam.service.chat;

import com.pickteam.domain.chat.ChatMember;
import com.pickteam.domain.chat.ChatMessage;
import com.pickteam.domain.chat.ChatRoom;
import com.pickteam.domain.enums.SseEventType;
import com.pickteam.domain.user.Account;
import com.pickteam.domain.workspace.Workspace;
import com.pickteam.dto.chat.ChatMemberJoinNotificationDto;
import com.pickteam.dto.chat.ChatMemberLeaveNotificationDto;
import com.pickteam.dto.chat.ChatMemberResponse;
import com.pickteam.repository.chat.ChatMemberRepository;
import com.pickteam.repository.chat.ChatMessageRepository;
import com.pickteam.repository.chat.ChatRoomRepository;
import com.pickteam.repository.user.AccountRepository;
import com.pickteam.repository.workspace.WorkspaceRepository;
import com.pickteam.service.sse.SseService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMemberServiceImpl implements ChatMemberService {

    private final ChatMemberRepository chatMemberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final WorkspaceRepository workspaceRepository;
    private final AccountRepository accountRepository;
    private final SseService sseService;
    private final ChatRoomService chatRoomService;

    @Override
    @Transactional
    public ChatMember joinChatRoom(Long accountId, Long workspaceId, Long chatId) {
        //입장 요청한 사용자가 워크스페이스 멤버인지 확인
        Workspace workspace = workspaceRepository.findByIdAndIsDeletedFalse(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("워크스페이스를 찾을 수 없습니다."));

        //채팅방이 존재하는지 확인
        chatRoomRepository.findByIdAndIsDeletedFalse(chatId)
                        .orElseThrow(()-> new EntityNotFoundException("채팅방이 존재하지 않습니다."));

        //채팅방에 이미 입장한 사용자인지 확인
        chatMemberRepository.findByChatRoomIdAndAccountIdAndIsDeletedFalse(chatId, accountId)
                .ifPresent(cm -> {
                    throw new IllegalStateException("이미 채팅방에 참여한 사용자입니다.");
                });

        //입장 처리
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        ChatMember chatMember = ChatMember.builder()
                .chatRoom(chatRoomRepository.findByIdAndIsDeletedFalse(chatId)
                        .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다.")))
                .account(account)
                .lastReadMessage(null)
                .build();

        ChatMember savedChatMember = chatMemberRepository.save(chatMember);

        notifyChatMemberJoined(chatId, accountId);

        return savedChatMember;
    }

    /**
     * 채팅방 멤버 입장을 다른 멤버들에게 알립니다.
     * 
     * @param chatRoomId 채팅방 ID
     * @param joinedAccountId 입장한 사용자 ID
     */
    private void notifyChatMemberJoined(Long chatRoomId, Long joinedAccountId) {
        // 채팅방의 활성 멤버 목록 조회
        List<ChatMember> activeMembers = chatMemberRepository.findAllByChatRoomIdAndIsDeletedFalse(chatRoomId);
        
        // 입장한 사용자 정보 조회
        Account joinedAccount = accountRepository.findById(joinedAccountId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        
        // 알림 DTO 생성
        ChatMemberJoinNotificationDto notificationDto = ChatMemberJoinNotificationDto.from(chatRoomId, joinedAccount);

        // 모든 활성 멤버에게 SSE 이벤트 전송
        activeMembers.forEach(member -> {
            sseService.sendToUser(
                member.getAccount().getId(),
                SseEventType.CHAT_MEMBER_JOINED.name(),
                notificationDto
            );
        });
    }

    @Transactional
    @Override
    public void leaveChatRoom(Long workspaceId, Long chatRoomId, Long accountId) {
        // chatRoom이 유효한지 체크
        ChatRoom chatRoom = chatRoomRepository.findByIdAndIsDeletedFalse(chatRoomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다."));

        //request로 날아온 accountId가 자기 자신의 것인지 체크하도록 리팩토링 해야 함

        //chatRoom에 accountId에 해당하는 chatMember가 있는지 체크
        ChatMember chatMember = chatMemberRepository.findByChatRoomIdAndAccountIdAndIsDeletedFalse(chatRoomId, accountId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방 멤버를 찾을 수 없습니다."));

        // chatRoom에서 사용자 제거 처리 (soft-delete)
        chatMember.markDeleted();
        chatMemberRepository.save(chatMember);

        notifyChatMemberLeft(chatRoomId, accountId);

        // 남은 활성 멤버가 있는지 확인
        List<ChatMember> remainingActiveMembers = chatMemberRepository.findAllByChatRoomIdAndIsDeletedFalse(chatRoomId);
        if (remainingActiveMembers.isEmpty()) {
            // 활성 멤버가 없으면 채팅방도 soft-delete 처리
            chatRoomService.deleteChatRoom(chatRoomId, workspaceId);
        }
    }

    /**
     * 채팅방 멤버 퇴장을 다른 멤버들에게 알립니다.
     * 
     * @param chatRoomId 채팅방 ID
     * @param leftAccountId 퇴장한 사용자 ID
     */
    private void notifyChatMemberLeft(Long chatRoomId, Long leftAccountId) {
        // 채팅방의 남은 활성 멤버 목록 조회
        List<ChatMember> remainingMembers = chatMemberRepository.findAllByChatRoomIdAndIsDeletedFalse(chatRoomId);
        
        // 퇴장한 사용자 정보 조회
        Account leftAccount = accountRepository.findById(leftAccountId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        
        // 알림 DTO 생성
        ChatMemberLeaveNotificationDto notificationDto = ChatMemberLeaveNotificationDto.from(chatRoomId, leftAccount);

        // 남은 활성 멤버들에게 SSE 이벤트 전송
        remainingMembers.forEach(member -> {
            sseService.sendToUser(
                member.getAccount().getId(),
                SseEventType.CHAT_MEMBER_LEFT.name(),
                notificationDto
            );
        });
    }

    @Override
    @Transactional
    public List<ChatMemberResponse> getChatMemberDetail(Long chatRoomId, Long accountId) {
        ChatRoom chatRoom = chatRoomRepository.findByIdAndIsDeletedFalse(chatRoomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다."));

        List<ChatMember> members = chatMemberRepository.findAllByChatRoomIdAndIsDeletedFalse(chatRoomId);
        return members.stream()
                .map(ChatMemberResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void kickChatMember(Long chatRoomId, Long targetAccountId, Long operatorAccountId) {
        //생각해보니 kick 기능이 있을필요가 없는 UX임 (제거 예정)
    }

    @Override
    @Transactional
    public void updateLastReadMessage(Long workspaceId, Long chatRoomId, Long accountId, Long messageId) {
        //유효성 검사
        // (chatRoom이 있는지,
        // account가 chatRoom에 속해있는지,
        // chatRoomId에 해당하는 방에 messageId에 해당하는 chatMessage가 있는지)
        //chatMember에서 chatRoomId와 accountId가 일치하는 대상의 lastReadMessage를 messageId로 설정
        ChatRoom chatRoom = chatRoomRepository.findByIdAndIsDeletedFalse(chatRoomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다."));

        ChatMember chatMember = chatMemberRepository.findByChatRoomIdAndAccountIdAndIsDeletedFalse(chatRoomId, accountId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방 멤버를 찾을 수 없습니다."));

        ChatMessage chatMessage = chatMessageRepository.findByIdAndChatRoomIdAndIsDeletedFalse(messageId, chatRoomId)
                .orElseThrow(() -> new EntityNotFoundException("메시지를 찾을 수 없습니다."));

        chatMember.setLastReadMessage(chatMessage);
        chatMemberRepository.save(chatMember);

    }

    @Override
    public List<ChatRoom> getChatRoomsByMember(Long accountId) {
        //현재 워크스페이스에서 accountId가 있는 채팅방 리스트 조회
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        List<ChatMember> members = chatMemberRepository.findAllByAccountIdAndIsDeletedFalse(accountId);
        return members.stream()
                .map(ChatMember::getChatRoom)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatMember> getChatMembers(Long chatRoomId) {
        chatRoomRepository.findByIdAndIsDeletedFalse(chatRoomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다."));

        return chatMemberRepository.findAllByChatRoomIdAndIsDeletedFalse(chatRoomId);
    }
}