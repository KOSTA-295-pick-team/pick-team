package com.pickteam.controller.kanban;

import com.pickteam.dto.ApiResponse;
import com.pickteam.dto.kanban.*;
import com.pickteam.security.UserPrincipal;
import com.pickteam.service.kanban.KanbanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/kanban")
@RequiredArgsConstructor
public class KanbanController {

    private final KanbanService kanbanService;

    @PostMapping
    public ResponseEntity<ApiResponse<KanbanDto>> createKanban(
            @Valid @RequestBody KanbanCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        KanbanDto kanban = kanbanService.createKanban(request);
        return ResponseEntity.ok(ApiResponse.success("칸반 보드가 생성되었습니다.", kanban));
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<ApiResponse<KanbanDto>> getKanbanByTeamId(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        KanbanDto kanban = kanbanService.getKanbanByTeamId(teamId);
        return ResponseEntity.ok(ApiResponse.success("칸반 보드를 조회했습니다.", kanban));
    }

    @PostMapping("/tasks")
    public ResponseEntity<ApiResponse<KanbanTaskDto>> createTask(
            @Valid @RequestBody KanbanTaskCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        KanbanTaskDto task = kanbanService.createKanbanTask(request);
        return ResponseEntity.ok(ApiResponse.success("칸반 태스크가 생성되었습니다.", task));
    }

    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<ApiResponse<KanbanTaskDto>> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody KanbanTaskUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        KanbanTaskDto task = kanbanService.updateKanbanTask(taskId, request);
        return ResponseEntity.ok(ApiResponse.success("칸반 태스크가 수정되었습니다.", task));
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        kanbanService.deleteKanbanTask(taskId);
        return ResponseEntity.ok(ApiResponse.success("칸반 태스크가 삭제되었습니다.", null));
    }

    @PostMapping("/tasks/{taskId}/comments")
    public ResponseEntity<ApiResponse<KanbanTaskCommentDto>> createComment(
            @PathVariable Long taskId,
            @Valid @RequestBody KanbanTaskCommentCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        request.setKanbanTaskId(taskId);
        KanbanTaskCommentDto comment = kanbanService.createComment(request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("댓글이 추가되었습니다.", comment));
    }

    // 댓글 수정 - /tasks/{taskId}/comments/{commentId} PUT
    @PutMapping("/tasks/{taskId}/comments/{commentId}")
    public ResponseEntity<ApiResponse<KanbanTaskCommentDto>> updateComment(
            @PathVariable Long taskId,
            @PathVariable Long commentId,
            @Valid @RequestBody KanbanTaskCommentUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        KanbanTaskCommentDto comment = kanbanService.updateComment(commentId, request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("댓글이 수정되었습니다.", comment));
    }

    // 댓글 삭제 - /tasks/{taskId}/comments/{commentId} DELETE
    @DeleteMapping("/tasks/{taskId}/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long taskId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        kanbanService.deleteComment(commentId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("댓글이 삭제되었습니다.", null));
    }

    // 댓글 페이징 조회 - /tasks/{taskId}/comments GET
    @GetMapping("/tasks/{taskId}/comments")
    public ResponseEntity<ApiResponse<Page<KanbanTaskCommentDto>>> getComments(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Pageable pageable = PageRequest.of(page, size);
        Page<KanbanTaskCommentDto> comments = kanbanService.getCommentsByTaskId(taskId, pageable);
        return ResponseEntity.ok(ApiResponse.success("댓글 목록을 조회했습니다.", comments));
    }
    
    // 칸반 리스트 생성 - /lists POST
    @PostMapping("/lists")
    public ResponseEntity<ApiResponse<KanbanListDto>> createList(
            @Valid @RequestBody KanbanListCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        KanbanListDto kanbanList = kanbanService.createKanbanList(request);
        return ResponseEntity.ok(ApiResponse.success("리스트가 생성되었습니다.", kanbanList));
    }
    
    // 칸반 리스트 수정 - /lists/{listId} PUT
    @PutMapping("/lists/{listId}")
    public ResponseEntity<ApiResponse<KanbanListDto>> updateList(
            @PathVariable Long listId,
            @Valid @RequestBody KanbanListUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        KanbanListDto kanbanList = kanbanService.updateKanbanList(listId, request);
        return ResponseEntity.ok(ApiResponse.success("리스트가 수정되었습니다.", kanbanList));
    }
    
    // 칸반 리스트 삭제 - /lists/{listId} DELETE
    @DeleteMapping("/lists/{listId}")
    public ResponseEntity<ApiResponse<Void>> deleteList(
            @PathVariable Long listId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        kanbanService.deleteKanbanList(listId);
        return ResponseEntity.ok(ApiResponse.success("리스트가 삭제되었습니다.", null));
    }
    
    // 작업 완료 요청 - /cards/{cardId}/completion-request POST
    @PostMapping("/cards/{cardId}/completion-request")
    public ResponseEntity<ApiResponse<KanbanTaskDto>> requestCompletion(
            @PathVariable Long cardId,
            @Valid @RequestBody KanbanTaskCompletionRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        KanbanTaskDto task = kanbanService.requestTaskCompletion(cardId, request);
        return ResponseEntity.ok(ApiResponse.success("작업 완료가 요청되었습니다.", task));
    }
    
    // 작업 완료 승인 - /cards/{cardId}/completion-approval POST
    @PostMapping("/cards/{cardId}/completion-approval")
    public ResponseEntity<ApiResponse<KanbanTaskDto>> approveCompletion(
            @PathVariable Long cardId,
            @Valid @RequestBody KanbanTaskCompletionApprovalRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        KanbanTaskDto task = kanbanService.approveTaskCompletion(cardId, request);
        return ResponseEntity.ok(ApiResponse.success("작업 완료가 승인되었습니다.", task));
    }
}