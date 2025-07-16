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
        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new RuntimeException("Team not found"));
        Workspace workspace = workspaceRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> new RuntimeException("Workspace not found"));

        Kanban kanban = Kanban.builder()
                .team(team)
                .workspace(workspace)
                .build();

        kanban = kanbanRepository.save(kanban);
        helper.createDefaultLists(kanban);
        return helper.convertToDto(kanban);
    }

    public KanbanDto getKanbanByTeamId(Long teamId) {
        Kanban kanban = kanbanRepository.findByTeamId(teamId)
                .orElseThrow(() -> new RuntimeException("Kanban not found for team: " + teamId));
        return helper.convertToDto(kanban);
    }

    @Transactional
    public KanbanListDto createKanbanList(KanbanListCreateRequest request) {
        Kanban kanban = kanbanRepository.findById(request.getKanbanId())
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
        KanbanList kanbanList = kanbanListRepository.findById(request.getKanbanListId())
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
        KanbanTask kanbanTask = kanbanTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("KanbanTask not found"));
        return helper.convertToDto(kanbanTask);
    }

    public List<KanbanTaskDto> getTasksByListId(Long listId) {
        return kanbanTaskRepository.findByKanbanListIdOrderByOrder(listId)
                .stream()
                .map(helper::convertToDto)
                .collect(Collectors.toList());
    }
}