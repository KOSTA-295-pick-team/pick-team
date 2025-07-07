package com.pickteam.controller.chat;

import com.pickteam.dto.ApiResponse;
import com.pickteam.dto.chat.ChatRoomCreateRequest;
import com.pickteam.dto.chat.ChatRoomDetailResponse;
import com.pickteam.dto.chat.ChatRoomResponse;
import com.pickteam.service.chat.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/chat-rooms")
@RequiredArgsConstructor
public class ChatRoomController {
    /**
     * 특정 워크스페이스에 속한 채팅방 목록을 조회합니다.
     */

    private final ChatRoomService chatRoomService;

    @GetMapping("/")
    Page<ChatRoomResponse> getChatRoomsByWorkspace(@PathVariable Long workspaceId, Pageable pageable) {
        return chatRoomService.getChatRoomsByWorkspace(workspaceId, pageable);
    }

    /**
     * 새로운 채팅방을 생성합니다.
     */
    @PostMapping("/")
    @PostMapping("/")
    public ResponseEntity createChatRoom(@AuthenticationPrincipal UserDetails userDetails,
                                         @RequestBody ChatRoomCreateRequest request) {
        Long creatorId = ((CustomUserDetails) userDetails).getId();
        ChatRoomResponse response = chatRoomService.createChatRoom(creatorId, request);
        return ResponseEntity.ok(ApiResponse.success("채팅방 생성 성공", response));
    }


    //---------------- Work in progress ------------------------------------------------------


    /**
     * 채팅방 제목을 수정합니다.
     */
    @PatchMapping("/{chatRoomId}")
    ChatRoomResponse updateChatRoomTitle(Long chatRoomId, String title) {

        return null;
    }

    /**
     * 1:1 채팅방을 생성합니다.
     */
    @PostMapping("/create-dm")
    ChatRoomResponse createDmChatRoom(Long workspaceId, Long memberId) {
        return null;
    }

    /**
     * 채팅방을 삭제합니다.
     * Soft-Delete 처리이므로 Patch 요청을 넣는다
     * 요청 경로는 restful하되 일반적인 수정 요청과 분리되도록 /delete suffix를 붙인다.
     */
    @PatchMapping("/{chatRoomId}/delete")
    void deleteChatRoom(Long chatRoomId, Long accountId) {

    }

    /**
     * ID로 채팅방 상세 정보를 조회합니다.
     */
    @GetMapping("/{chatRoomId}")
    ChatRoomDetailResponse getChatRoomDetails(Long chatRoomId) {
        return null;
    }

    /**
     * 채팅방 알림을 활성화합니다.
     */
    @PatchMapping("/{chatRoomId}/notification/enable")
    void enableChatRoomNotification(Long chatRoomId, Long accountId) {
    }

    /**
     * 채팅방 알림을 비활성화합니다.
     */
    @PatchMapping("/{chatRoomId}/notification/disable")
    void disableChatRoomNotification(Long chatRoomId, Long accountId) {

    }


}
