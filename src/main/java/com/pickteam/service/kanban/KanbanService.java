package com.pickteam.service.kanban;

import com.pickteam.domain.kanban.*;
import com.pickteam.domain.team.Team;
import com.pickteam.domain.workspace.Workspace;
import com.pickteam.dto.kanban.*;
import com.pickteam.repository.kanban.*;
import com.pickteam.repository.team.TeamRepository;
import com.pickteam.repository.workspace.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KanbanService {

    private final KanbanRepository kanbanRepository;
    private final KanbanListRepository kanbanListRepository;
    private final KanbanTaskRepository kanbanTaskRepository;
    private final TeamRepository teamRepository;
    private final WorkspaceRepository workspaceRepository;
    private final KanbanServiceHelper helper;

    @Transactional
    public KanbanDto createKanban(KanbanCreateRequest request) {
        // Soft Delete 고려한 일관성 있는 조회
        Team team = teamRepository.findByIdAndIsDeletedFalse(request.getTeamId())
                .orElseThrow(() -> new RuntimeException("Team not found"));
        Workspace workspace = workspaceRepository.findByIdAndIsDeletedFalse(request.getWorkspaceId())
                .orElseThrow(() -> new RuntimeException("Workspace not found"));

        Kanban kanban = Kanban.builder()
                .team(team)
                .workspace(workspace)
                .build();

        kanban = kanbanRepository.save(kanban);
        helper.createDefaultLists(kanban);
        return helper.convertToDto(kanban);
    }

    @Transactional(readOnly = true)
    public KanbanDto getKanbanByTeamId(Long teamId) {
        // 한 번에 team 검증과 kanban 조회로 중복 조회 방지
        Team team = teamRepository.findByIdAndIsDeletedFalse(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found: " + teamId));
        
        return kanbanRepository.findByTeamId(teamId)
                .map(helper::convertToDto)
                .orElseGet(() -> {
                    // 칸반이 없으면 자동 생성
                    try {
                        KanbanCreateRequest createRequest = new KanbanCreateRequest();
                        createRequest.setTeamId(teamId);
                        createRequest.setWorkspaceId(team.getWorkspace().getId());
                        return createKanban(createRequest);
                    } catch (Exception e) {
                        throw new RuntimeException("Kanban not found for team: " + teamId + " and failed to create automatically", e);
                    }
                });
    }

    @Transactional
    public KanbanListDto createKanbanList(KanbanListCreateRequest request) {
        Kanban kanban = kanbanRepository.findByIdAndIsDeletedFalse(request.getKanbanId())
                .orElseThrow(() -> new RuntimeException("Kanban not found"));

        Integer order = request.getOrder();
        if (order == null) {
            Integer maxOrder = kanbanListRepository.findMaxOrderByKanbanId(request.getKanbanId());
            order = (maxOrder != null) ? maxOrder + 1 : 0;
        }

        KanbanList kanbanList = KanbanList.builder()
                .kanbanListName(request.getKanbanListName())
                .kanban(kanban)
                .order(order)
                .build();

        kanbanList = kanbanListRepository.save(kanbanList);
        return helper.convertToDto(kanbanList);
    }

    @Transactional
    public KanbanTaskDto createKanbanTask(KanbanTaskCreateRequest request) {
        KanbanList kanbanList = kanbanListRepository.findByIdAndIsDeletedFalse(request.getKanbanListId())
                .orElseThrow(() -> new RuntimeException("KanbanList not found"));

        Integer order = request.getOrder();
        if (order == null) {
            Integer maxOrder = kanbanTaskRepository.findMaxOrderByKanbanListId(request.getKanbanListId());
            order = (maxOrder != null) ? maxOrder + 1 : 0;
        }

        KanbanTask kanbanTask = KanbanTask.builder()
                .subject(request.getSubject())
                .content(request.getContent())
                .deadline(request.getDeadline())
                .kanbanList(kanbanList)
                .order(order)
                .isApproved(false)
                .build();

        kanbanTask = kanbanTaskRepository.save(kanbanTask);

        if (request.getAssigneeIds() != null && !request.getAssigneeIds().isEmpty()) {
            helper.assignMembersToTask(kanbanTask.getId(), request.getAssigneeIds());
        }

        return helper.convertToDto(kanbanTask);
    }

    @Transactional
    public KanbanTaskDto updateKanbanTask(Long taskId, KanbanTaskUpdateRequest request) {
        return helper.updateKanbanTask(taskId, request);
    }

    @Transactional
    public KanbanTaskCommentDto createComment(KanbanTaskCommentCreateRequest request, Long authorId) {
        return helper.createComment(request, authorId);
    }

    // 댓글 수정
    @Transactional
    public KanbanTaskCommentDto updateComment(Long commentId, KanbanTaskCommentUpdateRequest request, Long userId) {
        return helper.updateComment(commentId, request, userId);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        helper.deleteComment(commentId, userId);
    }

    // 댓글 페이징 조회
    public Page<KanbanTaskCommentDto> getCommentsByTaskId(Long taskId, Pageable pageable) {
        return helper.getCommentsByTaskId(taskId, pageable);
    }

    // 칸반 리스트 관련 메서드들
    @Transactional
    public KanbanListDto updateKanbanList(Long listId, KanbanListUpdateRequest request) {
        return helper.updateKanbanList(listId, request);
    }

    @Transactional
    public void deleteKanbanList(Long listId) {
        helper.deleteKanbanList(listId);
    }
    
    // 작업 완료 요청/승인 관련 메서드들
    @Transactional
    public KanbanTaskDto requestTaskCompletion(Long cardId, KanbanTaskCompletionRequest request) {
        return helper.requestTaskCompletion(cardId, request);
    }
    
    @Transactional
    public KanbanTaskDto approveTaskCompletion(Long cardId, KanbanTaskCompletionApprovalRequest request) {
        return helper.approveTaskCompletion(cardId, request);
    }

    @Transactional
    public void deleteKanbanTask(Long taskId) {
        helper.deleteKanbanTask(taskId);
    }

    public KanbanTaskDto getKanbanTask(Long taskId) {
        KanbanTask kanbanTask = kanbanTaskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new RuntimeException("KanbanTask not found"));
        return helper.convertToDto(kanbanTask);
    }

    public List<KanbanTaskDto> getTasksByListId(Long listId) {
        return kanbanTaskRepository.findByKanbanListIdOrderByOrderWithFetch(listId)
                .stream()
                .map(helper::convertToDto)
                .collect(Collectors.toList());
    }
}