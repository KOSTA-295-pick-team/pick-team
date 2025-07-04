package com.pickteam.controller.kanban;

import com.pickteam.dto.ApiResponse;
import com.pickteam.dto.kanban.*;
import com.pickteam.security.UserPrincipal;
import com.pickteam.service.kanban.KanbanService;
import lombok.RequiredArgsConstructor;
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
} 