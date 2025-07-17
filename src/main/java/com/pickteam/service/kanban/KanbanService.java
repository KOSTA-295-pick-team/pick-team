package com.pickteam.service.kanban;

import com.pickteam.domain.kanban.*;
import com.pickteam.domain.team.Team;
import com.pickteam.domain.workspace.Workspace;
import com.pickteam.dto.kanban.*;
import com.pickteam.repository.kanban.*;
import com.pickteam.repository.team.TeamRepository;
import com.pickteam.repository.workspace.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class KanbanService {

    private final KanbanRepository kanbanRepository;
    private final KanbanListRepository kanbanListRepository;
    private final KanbanTaskRepository kanbanTaskRepository;
    private final TeamRepository teamRepository;
    private final WorkspaceRepository workspaceRepository;
    private final KanbanServiceHelper helper;

    @Transactional
    public KanbanDto createKanban(KanbanCreateRequest request) {
        Team team = teamRepository.findByIdAndIsDeletedFalse(request.getTeamId())
                .orElseThrow(() -> new RuntimeException("Team not found"));
        Workspace workspace = workspaceRepository.findByIdAndIsDeletedFalse(request.getWorkspaceId())
                .orElseThrow(() -> new RuntimeException("Workspace not found"));

        // 다음 순서 계산
        List<Kanban> existingKanbans = kanbanRepository.findByTeamId(request.getTeamId());
        int nextOrder = existingKanbans.isEmpty() ? 0 : 
                       existingKanbans.stream()
                           .mapToInt(k -> k.getOrder() != null ? k.getOrder() : 0)
                           .max().orElse(0) + 1;

        Kanban kanban = Kanban.builder()
                .name(request.getName() != null ? request.getName() : "칸반")
                .order(nextOrder)
                .team(team)
                .workspace(workspace)
                .build();

        kanban = kanbanRepository.save(kanban);
        helper.createDefaultLists(kanban);
        return helper.convertToDto(kanban);
    }

    @Transactional
    public KanbanDto getKanbanByTeamId(Long teamId) {
        Team team = teamRepository.findByIdAndIsDeletedFalse(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found: " + teamId));
        
        // 첫 번째 칸반 조회 (Pageable 사용)
        List<Kanban> kanbans = kanbanRepository.findFirstByTeamId(teamId, PageRequest.of(0, 1));
        
        if (!kanbans.isEmpty()) {
            return helper.convertToDto(kanbans.get(0));
        }
        
        // 칸반이 없으면 자동 생성
        return createKanbanForTeam(teamId, team.getWorkspace().getId());
    }

    @Transactional
    public KanbanDto createKanbanForTeam(Long teamId, Long workspaceId) {
        // 동시성 문제 방지를 위해 다시 한 번 확인
        List<Kanban> existingKanbans = kanbanRepository.findFirstByTeamId(teamId, PageRequest.of(0, 1));
        if (!existingKanbans.isEmpty()) {
            log.info("Kanban already exists for team: {}, returning existing kanban", teamId);
            return helper.convertToDto(existingKanbans.get(0));
        }
        
        try {
            KanbanCreateRequest createRequest = new KanbanCreateRequest();
            createRequest.setTeamId(teamId);
            createRequest.setWorkspaceId(workspaceId);
            createRequest.setName("기본 칸반");
            return createKanban(createRequest);
            
        } catch (Exception e) {
            // 동시성 문제로 생성 실패 시 다시 조회
            log.warn("Failed to create kanban for team: {}, attempting to find existing", teamId);
            List<Kanban> kanbans = kanbanRepository.findFirstByTeamId(teamId, PageRequest.of(0, 1));
            if (!kanbans.isEmpty()) {
                return helper.convertToDto(kanbans.get(0));
            }
            throw new RuntimeException("Kanban creation failed for team: " + teamId, e);
        }
    }

    // 미래 확장을 위한 메소드
    @Transactional(readOnly = true)
    public List<KanbanDto> getAllKanbansByTeamId(Long teamId) {
        return kanbanRepository.findByTeamId(teamId)
                .stream()
                .map(helper::convertToDto)
                .collect(Collectors.toList());
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

    @Transactional
    public KanbanTaskCommentDto updateComment(Long commentId, KanbanTaskCommentUpdateRequest request, Long userId) {
        return helper.updateComment(commentId, request, userId);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        helper.deleteComment(commentId, userId);
    }

    public Page<KanbanTaskCommentDto> getCommentsByTaskId(Long taskId, Pageable pageable) {
        return helper.getCommentsByTaskId(taskId, pageable);
    }

    @Transactional
    public KanbanListDto updateKanbanList(Long listId, KanbanListUpdateRequest request) {
        return helper.updateKanbanList(listId, request);
    }

    @Transactional
    public void deleteKanbanList(Long listId) {
        helper.deleteKanbanList(listId);
    }
    
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