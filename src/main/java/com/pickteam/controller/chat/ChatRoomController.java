package com.pickteam.controller.chat;

import com.pickteam.domain.chat.ChatMember;
import com.pickteam.domain.chat.ChatRoom;
import com.pickteam.dto.ApiResponse;
import com.pickteam.dto.chat.*;
import com.pickteam.service.chat.ChatMemberService;
import com.pickteam.service.chat.ChatMessageService;
import com.pickteam.service.chat.ChatRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/chat-rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final ChatMemberService chatMemberService;

    /**
     * 워크스페이스의 채팅방 목록을 조회합니다.
     *
     * @param workspaceId 워크스페이스 ID
     * @param pageable    페이징 정보
     * @return 채팅방 목록 페이지
     */
    @GetMapping("/")
    public ResponseEntity<ApiResponse<Page<ChatRoomResponse>>> getChatRoomsByWorkspace(
            @PathVariable Long workspaceId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ChatRoomResponse> chatRooms = chatRoomService.getChatRoomsByWorkspace(workspaceId, pageable);
        return ResponseEntity.ok(ApiResponse.success("채팅방 목록 조회 성공", chatRooms));
    }

    /**
     * 새로운 채팅방을 생성합니다.
     *
     * @param creatorId 생성자 ID
     * @param request   채팅방 생성 요청
     * @return 생성된 채팅방 정보
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createChatRoom(@RequestParam Long creatorId,
                                                                        @RequestBody ChatRoomCreateRequest request) {
        ChatRoomResponse response = chatRoomService.createChatRoom(creatorId, request);
        return ResponseEntity.ok(ApiResponse.success("채팅방 생성 성공", response));
    }

    /**
     * 채팅방 제목을 변경합니다.
     *
     * @param requestUserId 요청 사용자 ID
     * @param request       채팅방 제목 변경 요청
     * @param workspaceId   워크스페이스 ID
     * @param chatRoomId    채팅방 ID
     * @return 변경된 채팅방 정보
     */
    @PatchMapping("/{chatRoomId}/updateTitle")
    ResponseEntity<ApiResponse<ChatRoomResponse>> updateChatRoomTitle(@RequestParam Long requestUserId,
                                                                      @RequestBody ChatRoomUpdateTitleRequest request,
                                                                      @PathVariable Long workspaceId,
                                                                      @PathVariable Long chatRoomId
    ) {
        ChatRoomResponse response = chatRoomService.updateChatRoomTitle(requestUserId, request, workspaceId, chatRoomId);
        return ResponseEntity.ok(ApiResponse.success("채팅방 제목 변경 성공", response));
    }


    /**
     * DM 채팅방을 생성합니다.
     *
     * @param creatorId 생성자 ID
     * @param request   DM 채팅방 생성 요청
     * @return 생성된 DM 채팅방 정보
     */
    @PostMapping("/create-dm")
    ResponseEntity<ApiResponse<ChatRoomResponse>> createDmChatRoom(@RequestParam Long creatorId,
                                                                   @RequestBody ChatRoomCreateRequest request) {
        ChatRoomResponse response = chatRoomService.createDmChatRoom(creatorId, request);
        return ResponseEntity.ok(ApiResponse.success("채팅방 생성 성공", response));
    }


    /**
     * 최근 메시지를 조회합니다.
     *
     * @param chatRoomId 채팅방 ID
     * @param pageable   페이징 정보
     * @return 메시지 목록
     */
    @GetMapping("/{chatRoomId}/messages")
    public ResponseEntity<ChatMessageListResponse> getRecentMessages(
            @PathVariable Long chatRoomId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {

        // 최신순 DESC로 받아온 후 ASC 정렬해서 리턴하는 방식은 서비스에서 처리함
        ChatMessageListResponse messages = chatMessageService.getRecentMessages(chatRoomId, pageable);
        return ResponseEntity.ok(messages);
    }


    /**
     * 새 메시지를 전송합니다.
     *
     * @param chatRoomId 채팅방 ID
     * @param request    메시지 전송 요청
     * @return 전송된 메시지 정보
     */
    @PostMapping("/{chatRoomId}/messages")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @PathVariable Long chatRoomId,
            @RequestBody @Valid ChatMessageRequest request
    ) {
        ChatMessageResponse response = chatMessageService.sendMessage(chatRoomId, request);
        return ResponseEntity.ok(response);
    }


    /**
     * 채팅방에 참여합니다.
     *
     * @param workspaceId 워크스페이스 ID
     * @param chatRoomId  채팅방 ID
     * @param accountId   사용자 ID
     * @return 참여 멤버 정보
     */
    @PostMapping("/{chatRoomId}/join")
    public ResponseEntity<ApiResponse<ChatMemberResponse>> joinChatRoom(
            @PathVariable Long workspaceId,
            @PathVariable Long chatRoomId,
            @RequestParam Long accountId
    ) {
        ChatMember joinedMember = chatMemberService.joinChatRoom(accountId, workspaceId, chatRoomId);
        return ResponseEntity.ok(ApiResponse.success("채팅방 입장 성공", ChatMemberResponse.from(joinedMember)));
    }

    /**
     * 채팅방을 퇴장합니다.
     *
     * @param workspaceId 워크스페이스 ID
     * @param chatRoomId  채팅방 ID
     * @param accountId   사용자 ID
     * @return 성공 여부
     */
    @PatchMapping("/{chatRoomId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveChatRoom(
            @PathVariable Long workspaceId,
            @PathVariable Long chatRoomId,
            @RequestParam Long accountId
    ) {
        chatMemberService.leaveChatRoom(workspaceId, chatRoomId, accountId);
        return ResponseEntity.ok(ApiResponse.success("채팅방 퇴장 성공", null));
    }


    /**
     * 마지막으로 읽은 메시지를 갱신합니다.
     *
     * @param workspaceId 워크스페이스 ID
     * @param chatRoomId  채팅방 ID
     * @param accountId   사용자 ID
     * @param messageId   마지막으로 읽은 메시지 ID
     * @return 성공 여부
     */
    @PatchMapping("/{chatRoomId}/last-read-refresh")
    public ResponseEntity<ApiResponse<Void>> updateLastReadMessage(
            @PathVariable Long workspaceId,
            @PathVariable Long chatRoomId,
            @RequestParam Long accountId,
            @RequestParam Long messageId
    ) {
        chatMemberService.updateLastReadMessage(workspaceId, chatRoomId, accountId, messageId);
        return ResponseEntity.ok(ApiResponse.success("마지막 읽은 메시지 갱신 완료", null));
    }

    /**
     * 채팅방 멤버 목록을 조회합니다.
     *
     * @param chatRoomId 채팅방 ID
     * @return 채팅방 멤버 목록
     */
    @GetMapping("/{chatRoomId}/members")
    public ResponseEntity<ApiResponse<List<ChatMemberResponse>>> getMembers(
            @PathVariable Long chatRoomId
    ) {
        List<ChatMemberResponse> members = chatMemberService.getChatMembers(chatRoomId).stream()
                .map(ChatMemberResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("채팅방 멤버 조회 성공", members));
    }

    /**
     * 내가 참여한 채팅방 목록을 조회합니다.
     *
     * @param accountId 사용자 ID
     * @return 참여 중인 채팅방 목록
     */
    @GetMapping("/accounts/{accountId}/")
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getMyChatRooms(
            @PathVariable Long accountId
    ) {
        List<ChatRoom> chatRooms = chatMemberService.getChatRoomsByMember(accountId);
        List<ChatRoomResponse> response = chatRooms.stream()
                .map(ChatRoomResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("참여 중인 채팅방 조회 성공", response));
    }

    //---------------- Work in progress ------------------------------------------------------

    /**
     * 채팅방을 삭제합니다.
     * Soft-Delete 처리이므로 Patch 요청을 넣는다
     * 요청 경로는 restful하되 일반적인 수정 요청과 분리되도록 /delete suffix를 붙인다.
     */
    //TODO : 임시로 선언만 해둔 메소드이며 구현 예정임 (WIP)
    @PatchMapping("/{chatRoomId}/delete")
    void deleteChatRoom(Long chatRoomId, Long accountId) {

    }

    /**
     * ID로 채팅방 상세 정보를 조회합니다.
     */
    //TODO : 임시로 선언만 해둔 메소드이며 구현 예정임 (WIP)
    @GetMapping("/{chatRoomId}")
    ChatRoomDetailResponse getChatRoomDetails(Long chatRoomId) {
        return null;
    }


    /**
     * 채팅방 알림을 활성화합니다.
     */
    //TODO : 임시로 선언만 해둔 메소드이며 구현 예정임 (WIP)
    @PatchMapping("/{chatRoomId}/notification/enable")
    void enableChatRoomNotification(Long chatRoomId, Long accountId) {
    }


    /**
     * 채팅방 알림을 비활성화합니다.
     */
    //TODO : 임시로 선언만 해둔 메소드이며 구현 예정임 (WIP)
    @PatchMapping("/{chatRoomId}/notification/disable")
    void disableChatRoomNotification(Long chatRoomId, Long accountId) {

    }


}