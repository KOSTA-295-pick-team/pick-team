package com.pickteam.controller.chat;

import com.pickteam.dto.ApiResponse;
import com.pickteam.dto.chat.ChatRoomCreateRequest;
import com.pickteam.dto.chat.ChatRoomDetailResponse;
import com.pickteam.dto.chat.ChatRoomResponse;
import com.pickteam.dto.chat.ChatRoomUpdateTitleRequest;
import com.pickteam.service.chat.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
     */
    //creatorId를 RequestParam으로 받아오게 되어있는데, 리팩토링 필요
    //현재 로그인 정보이므로 RequestParam으로 받아오기보단 @AuthenticationPrincipal 사용하는 것이 바람직하다...
    //다른 컨트롤러 구현과 맞춰 일단 RequestParam으로 구현하고 추후 리팩토링한다.
    @PostMapping("/")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createChatRoom(@RequestParam Long creatorId,
                                         @RequestBody ChatRoomCreateRequest request) {
        ChatRoomResponse response = chatRoomService.createChatRoom(creatorId, request);
        System.out.println("📦 workspaceId (path) = " + request.getWorkspaceId());
        System.out.println("📥 request = " + request);
        System.out.println("🧾 memberIds = " + request.getChatMemberIdList());
        return ResponseEntity.ok(ApiResponse.success("채팅방 생성 성공", response));
    }


    /**
     * 채팅방 제목을 수정합니다.
     */

    @PatchMapping("/")
    ResponseEntity<ApiResponse<ChatRoomResponse>> updateChatRoomTitle(@RequestParam Long requestUserId,
                                                                      @RequestBody ChatRoomUpdateTitleRequest request,
                                                                      @PathVariable Long workspaceId)
    {
        System.out.println(request.getWorkspaceId());
        if (!workspaceId.equals(request.getWorkspaceId())) {
            //PathVariable로 선언했으면 내부 어딘가에서 사용해줘야 한다.
            //ChatRoom이 Workspace 정보를 들고 있으므로 여기서 경로를 넘겨주지 않아도 서비스 레이어에서 검증이 가능하다.
            //별 의미는 없을 것 같지만 여기서 유효성 체크 한번 더 진행하는 것으로 값을 한번 사용해준다.
            throw new IllegalArgumentException("경로와 바디의 워크스페이스 ID가 다릅니다.");
        }
        ChatRoomResponse response = chatRoomService.updateChatRoomTitle(requestUserId, request);
        return ResponseEntity.ok(ApiResponse.success("채팅방 제목 변경 성공",response));
    }

    /**
     * 1:1 채팅방을 생성합니다.
     */
    @PostMapping("/create-dm")
    ResponseEntity<ApiResponse<ChatRoomResponse>> createDmChatRoom(@RequestParam Long creatorId,
                                      @RequestBody ChatRoomCreateRequest request) {
        ChatRoomResponse response = chatRoomService.createDmChatRoom(creatorId, request);
        return ResponseEntity.ok(ApiResponse.success("채팅방 생성 성공", response));
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
