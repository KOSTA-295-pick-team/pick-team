package com.pickteam.service.chat;

import com.pickteam.domain.chat.ChatMember;
import com.pickteam.domain.chat.ChatRoom;
import com.pickteam.domain.chat.ChatMessage;
import com.pickteam.domain.workspace.Workspace;
import com.pickteam.dto.chat.ChatRoomCreateRequest;
import com.pickteam.dto.chat.ChatRoomDetailResponse;
import com.pickteam.dto.chat.ChatRoomResponse;
import com.pickteam.repository.chat.ChatMemberRepository;
import com.pickteam.repository.chat.ChatRoomRepository;
import com.pickteam.repository.team.TeamRepository;
import com.pickteam.repository.user.AccountRepository;
import com.pickteam.repository.workspace.WorkspaceRepository;
import com.pickteam.service.WorkspaceService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService{

    //자신의 Repository 선언
    private final ChatRoomRepository chatRoomRepository;

    //내부에서 호출하는 Service 선언
    private final WorkspaceService workspaceService;

    // Repository 직접 선언
    // 추후 해당 Service 영역에 엔티티 받아오는 메소드 선언해서 서비스를 타는 방식으로 리팩토링하는게 좋을 듯
    // 동작은 하지만 관심사분리가 안 되어서 유지보수에 이슈 생길 수도 있음

    private final WorkspaceRepository workspaceRepository;
    private final AccountRepository accountRepository;
    private final TeamRepository teamRepository;
    private final ChatMemberRepository chatMemberRepository;

    @Override
    public Page<ChatRoomResponse> getChatRoomsByWorkspace(Long workspaceId, Pageable pageable) {

        return null;
    }

    @Override
    @Transactional
public ChatRoomResponse createChatRoom(Long creatorId, ChatRoomCreateRequest request) {
    Workspace workspace = workspaceRepository.findByIdAndIsDeletedFalse(request.getWorkspaceId())
            .orElseThrow(() -> new EntityNotFoundException("해당하는 워크스페이스가 없습니다."));

    // 생성자가 워크스페이스 멤버인지 확인
    if (!workspaceService.isWorkspaceMember(workspace.getId(), creatorId)) {
        throw new IllegalArgumentException("워크스페이스 멤버만 채팅방을 생성할 수 있습니다.");
    }

    List<Long> memberIds = request.getChatMemberIdLists();
    // 중복 제거 및 생성자 포함 보장
    Set<Long> uniqueMemberIds = new HashSet<>(memberIds);
    uniqueMemberIds.add(creatorId);

    List<ChatMember> chatMembers = new ArrayList<>();

    ChatRoom chatroom = ChatRoom.builder()
            .name(request.getName())
            .type(request.getType())
            .workspace(workspace)
            .chatMembers(new ArrayList<ChatMember>())
            .chatMessages(new ArrayList<ChatMessage>())
            .build();
    chatroom = chatRoomRepository.save(chatroom);

    // uniqueMemberIds 순회하면서 ChatMember 생성
    for (Long memberId : uniqueMemberIds) {
        ChatMember chatMember = ChatMember.builder()
                .account(accountRepository.findById(memberId)
                        .orElseThrow(() -> new EntityNotFoundException("대상 멤버를 찾을 수 없습니다.")))
                .chatRoom(chatroom)
                .lastReadMessage(null)
                .build();
        chatMembers.add(chatMember);
    }

    chatMembers = chatMemberRepository.saveAll(chatMembers);

    return ChatRoomResponse.from(chatroom);
}

    @Override
    public ChatRoomResponse updateChatRoomTitle(Long chatRoomId, String title) {
        return null;
    }

    @Override
    public ChatRoomResponse createDmChatRoom(Long workspaceId, Long memberId) {
        return null;
    }

    @Override
    public void deleteChatRoom(Long chatRoomId, Long accountId) {

    }

    @Override
    public ChatRoomDetailResponse getChatRoomDetails(Long chatRoomId) {
        return null;
    }

    @Override
    public void enableChatRoomNotification(Long chatRoomId, Long accountId) {

    }

    @Override
    public void disableChatRoomNotification(Long chatRoomId, Long accountId) {

    }

}