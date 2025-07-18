package com.pickteam.service.chat;

import com.pickteam.domain.chat.ChatMember;
import com.pickteam.domain.chat.ChatRoom;
import com.pickteam.domain.chat.ChatMessage;
import com.pickteam.domain.enums.ChatRoomType;
import com.pickteam.domain.workspace.Workspace;
import com.pickteam.domain.workspace.WorkspaceMember;
import com.pickteam.dto.chat.ChatRoomCreateRequest;
import com.pickteam.dto.chat.ChatRoomDetailResponse;
import com.pickteam.dto.chat.ChatRoomResponse;
import com.pickteam.dto.chat.ChatRoomUpdateTitleRequest;
import com.pickteam.repository.chat.ChatMemberRepository;
import com.pickteam.repository.chat.ChatRoomRepository;
import com.pickteam.repository.team.TeamRepository;
import com.pickteam.repository.user.AccountRepository;
import com.pickteam.repository.workspace.WorkspaceMemberRepository;
import com.pickteam.repository.workspace.WorkspaceRepository;
import com.pickteam.service.WorkspaceService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {

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
    private final WorkspaceMemberRepository workspaceMemberRepository;

    /**
     * 채팅방 목록 읽어오기
     *
     * @param workspaceId
     * @param pageable
     * @return
     */
    @Override
    public Page<ChatRoomResponse> getChatRoomsByWorkspace(Long workspaceId, Pageable pageable) {
        // 워크스페이스 존재 확인
        Workspace workspace = workspaceRepository.findByIdAndIsDeletedFalse(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("워크스페이스를 찾을 수 없습니다."));

        // 페이징된 채팅방 목록 조회
        Page<ChatRoom> chatRoomPage = chatRoomRepository.findByWorkspaceAndIsDeletedFalse(workspace, pageable);

        // ChatRoomResponse로 변환
        return chatRoomPage.map(ChatRoomResponse::from);
    }

    /**
     * 채팅방 생성
     *
     * @param creatorId
     * @param request
     * @return
     */
    @Override
    @Transactional
    public ChatRoomResponse createChatRoom(Long creatorId, ChatRoomCreateRequest request) {
        Workspace workspace = workspaceRepository.findByIdAndIsDeletedFalse(request.getWorkspaceId())
                .orElseThrow(() -> new EntityNotFoundException("해당하는 워크스페이스가 없습니다."));

        //creator가 해당 워크스페이스 멤버인지 확인
        List<WorkspaceMember> memberList = workspaceMemberRepository
                .findByAccountIdAndStatus(creatorId, WorkspaceMember.MemberStatus.ACTIVE);

        boolean isMember = memberList.stream()
                .anyMatch(m -> m.getWorkspace().getId().equals(request.getWorkspaceId()));

        if (!isMember) {
            throw new IllegalArgumentException("워크스페이스 멤버가 아닙니다.");
        }

        List<Long> memberIds = request.getChatMemberIdList();
        List<ChatMember> chatMembers = new ArrayList<>();

        //중복 멤버 검사 (멤버 목록 사이즈와 해시셋으로 뽑은 중복 제거 사이즈를 비교)
        if (memberIds.size() != new HashSet<>(memberIds).size()) {
            throw new IllegalArgumentException("채팅방 멤버에 중복된 사용자가 포함되어 있습니다.");
        }

        ChatRoom chatroom = ChatRoom.builder()
                .name(request.getName())
                .type(request.getType())
                .workspace(workspace)
                .chatMembers(new ArrayList<ChatMember>())
                .chatMessages(new ArrayList<ChatMessage>())
                .build();
        chatroom = chatRoomRepository.save(chatroom);
        //memberIds 순회하면서 ChatMember 생성
        for (Long memberId : memberIds) {
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


    /**
     * 채팅방 제목 변경
     *
     * @param requestUserId
     * @param request
     * @return
     */
    @Override
    @Transactional
    public ChatRoomResponse updateChatRoomTitle(Long requestUserId, ChatRoomUpdateTitleRequest request,
                                                Long workspaceId, Long chatRoomId) {
        //workspaceId가 유효한지 검사
        Workspace workspace = workspaceRepository.findByIdAndIsDeletedFalse(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("워크스페이스를 찾을 수 없습니다."));

        //requestUserId가 chatRoom안에 속해 있는지 검사
        ChatRoom chatroom = (chatRoomRepository.findByIdAndIsDeletedFalse(chatRoomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다")));

        //채팅방의 멤버라면 채팅방 제목을 변경 처리
        //방장 여부나 방장을 검사하는 로직이 별도로 없으므로 누구나 변경 가능하도록 처리
        //방장만 변경 가능하도록 하려면 구조 확장 필요
        boolean isMember = chatroom.getChatMembers().stream()
                .anyMatch(m -> m.getAccount().getId().equals(requestUserId));

        if (isMember) chatroom.setName(request.getNewName());//채팅방 이름 변경처리
        else throw new EntityNotFoundException("채팅방의 멤버가 아닙니다.");
        return ChatRoomResponse.from(chatroom);
    }

    /**
     * 1:1 채팅방 생성
     *
     * @param creatorId
     * @param request
     * @return
     */
    @Override
    @Transactional
    public ChatRoomResponse createDmChatRoom(Long creatorId, ChatRoomCreateRequest request) {
        //채팅방 생성
        //내부적으로 CreateChatRoom을 호출하는 방식으로 구현
        //추후 별도 로직이 붙을 경우를 대비해 메소드 분리만 해둠
        ChatRoomResponse response = createChatRoom(creatorId, request);
        return response;
    }

    //채팅방 삭제
    @Override
    @Transactional
    public void deleteChatRoom(Long chatRoomId, Long workspaceId) {
        //채팅방 삭제
        //방장이 직접 삭제하는 경우는 없고, 모든 사용자가 나갔을 때 이 메소드를 통해 삭제 요청이 온다.
        Workspace workspace = workspaceRepository.findByIdAndIsDeletedFalse(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("워크스페이스를 찾을 수 없습니다."));

        ChatRoom chatroom = chatRoomRepository.findByIdAndIsDeletedFalse(chatRoomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다"));

        chatroom.markDeleted();
        chatRoomRepository.save(chatroom);


    }
    // ------------------------- Work in Progress --------------------------------

    //TODO : 임시로 선언만 해둔 메소드이며 구현 예정임 (WIP)
    //채팅방 상세정보 가져오기
    //채팅방에 참여중인 인원 정보와 채팅 내역 불러오기
    @Override
    public ChatRoomDetailResponse getChatRoomDetails(Long chatRoomId) {


        return null;
    }

    //TODO : 임시로 선언만 해둔 메소드이며 구현 예정임 (WIP)
    //채팅방 알림 켜기
    @Override
    public void enableChatRoomNotification(Long chatRoomId, Long accountId) {

    }

    //TODO : 임시로 선언만 해둔 메소드이며 구현 예정임 (WIP)
    //채팅방 알림 끄기
    @Override
    public void disableChatRoomNotification(Long chatRoomId, Long accountId) {

    }

}