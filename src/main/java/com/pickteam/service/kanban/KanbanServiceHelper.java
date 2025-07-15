package com.pickteam.service.kanban;

import com.pickteam.domain.kanban.*;
import com.pickteam.domain.user.Account;
import com.pickteam.dto.kanban.*;
import com.pickteam.repository.kanban.*;
import com.pickteam.repository.user.AccountRepository;
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
public class KanbanServiceHelper {

    private final KanbanListRepository kanbanListRepository;
    private final KanbanTaskRepository kanbanTaskRepository;
    private final KanbanTaskCommentRepository kanbanTaskCommentRepository;
    private final KanbanTaskMemberRepository kanbanTaskMemberRepository;
    private final KanbanTaskAttachRepository kanbanTaskAttachRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public KanbanTaskDto updateKanbanTask(Long taskId, KanbanTaskUpdateRequest request) {
        KanbanTask kanbanTask = kanbanTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("KanbanTask not found"));

        if (request.getSubject() != null) kanbanTask.setSubject(request.getSubject());
        if (request.getContent() != null) kanbanTask.setContent(request.getContent());
        if (request.getDeadline() != null) kanbanTask.setDeadline(request.getDeadline());
        if (request.getOrder() != null) kanbanTask.setOrder(request.getOrder());
        if (request.getIsApproved() != null) kanbanTask.setIsApproved(request.getIsApproved());

        if (request.getKanbanListId() != null) {
            KanbanList newKanbanList = kanbanListRepository.findById(request.getKanbanListId())
                    .orElseThrow(() -> new RuntimeException("KanbanList not found"));
            kanbanTask.setKanbanList(newKanbanList);
        }

        if (request.getAssigneeIds() != null) {
            updateTaskMembers(taskId, request.getAssigneeIds());
        }

        kanbanTask = kanbanTaskRepository.save(kanbanTask);
        return convertToDto(kanbanTask);
    }

    @Transactional
    public KanbanTaskCommentDto createComment(KanbanTaskCommentCreateRequest request, Long authorId) {
        KanbanTask kanbanTask = kanbanTaskRepository.findById(request.getKanbanTaskId())
                .orElseThrow(() -> new RuntimeException("KanbanTask not found"));
        Account author = accountRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        KanbanTaskComment comment = KanbanTaskComment.builder()
                .comment(request.getComment())
                .kanbanTask(kanbanTask)
                .account(author)
                .build();

        comment = kanbanTaskCommentRepository.save(comment);
        return convertToDto(comment);
    }

    @Transactional
    public void deleteKanbanTask(Long taskId) {
        KanbanTask kanbanTask = kanbanTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("KanbanTask not found"));
        // Soft delete using BaseSoftDeleteSupportEntity
        kanbanTask.markDeleted();
        kanbanTaskRepository.save(kanbanTask);
    }

    @Transactional
    public void assignMembersToTask(Long taskId, List<Long> accountIds) {
        KanbanTask kanbanTask = kanbanTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("KanbanTask not found"));

        for (Long accountId : accountIds) {
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new RuntimeException("Account not found"));

            KanbanTaskMember member = KanbanTaskMember.builder()
                    .kanbanTask(kanbanTask)
                    .account(account)
                    .build();
            kanbanTaskMemberRepository.save(member);
        }
    }

    @Transactional
    public void updateTaskMembers(Long taskId, List<Long> accountIds) {
        List<KanbanTaskMember> existingMembers = kanbanTaskMemberRepository.findByKanbanTaskId(taskId);
        for (KanbanTaskMember member : existingMembers) {
            member.markDeleted();
            kanbanTaskMemberRepository.save(member);
        }

        if (!accountIds.isEmpty()) {
            assignMembersToTask(taskId, accountIds);
        }
    }

    public void createDefaultLists(Kanban kanban) {
        List<String> defaultListNames = List.of("To Do", "In Progress", "Done");
        for (int i = 0; i < defaultListNames.size(); i++) {
            KanbanList kanbanList = KanbanList.builder()
                    .kanbanListName(defaultListNames.get(i))
                    .kanban(kanban)
                    .order(i)
                    .build();
            kanbanListRepository.save(kanbanList);
        }
    }

    // DTO 변환 메서드들
    public KanbanDto convertToDto(Kanban kanban) {
        List<KanbanListDto> kanbanListDtos = kanbanListRepository.findByKanbanIdOrderByOrder(kanban.getId())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return KanbanDto.builder()
                .id(kanban.getId())
                .teamId(kanban.getTeam().getId())
                .workspaceId(kanban.getWorkspace().getId())
                .kanbanLists(kanbanListDtos)
                .createdAt(kanban.getCreatedAt())
                .updatedAt(kanban.getUpdatedAt())
                .build();
    }

    public KanbanListDto convertToDto(KanbanList kanbanList) {
        List<KanbanTaskDto> taskDtos = kanbanTaskRepository.findByKanbanListIdOrderByOrder(kanbanList.getId())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return KanbanListDto.builder()
                .id(kanbanList.getId())
                .kanbanListName(kanbanList.getKanbanListName())
                .kanbanId(kanbanList.getKanban().getId())
                .tasks(taskDtos)
                .order(kanbanList.getOrder())
                .createdAt(kanbanList.getCreatedAt())
                .updatedAt(kanbanList.getUpdatedAt())
                .build();
    }

    public KanbanTaskDto convertToDto(KanbanTask kanbanTask) {
        List<KanbanTaskCommentDto> commentDtos = kanbanTaskCommentRepository.findByKanbanTaskIdOrderByCreatedAt(kanbanTask.getId())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        List<KanbanTaskMemberDto> memberDtos = kanbanTaskMemberRepository.findByKanbanTaskId(kanbanTask.getId())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        List<KanbanTaskAttachDto> attachDtos = kanbanTaskAttachRepository.findByKanbanTaskId(kanbanTask.getId())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return KanbanTaskDto.builder()
                .id(kanbanTask.getId())
                .subject(kanbanTask.getSubject())
                .content(kanbanTask.getContent())
                .deadline(kanbanTask.getDeadline())
                .kanbanListId(kanbanTask.getKanbanList().getId())
                .order(kanbanTask.getOrder())
                .isApproved(kanbanTask.getIsApproved())
                .completionRequested(kanbanTask.getCompletionRequested())
                .completionRequestMessage(kanbanTask.getCompletionRequestMessage())
                .comments(commentDtos)
                .members(memberDtos)
                .attachments(attachDtos)
                .createdAt(kanbanTask.getCreatedAt())
                .updatedAt(kanbanTask.getUpdatedAt())
                .build();
    }

    public KanbanTaskCommentDto convertToDto(KanbanTaskComment comment) {
        return KanbanTaskCommentDto.builder()
                .id(comment.getId())
                .comment(comment.getComment())
                .kanbanTaskId(comment.getKanbanTask().getId())
                .accountId(comment.getAccount().getId())
                .authorName(comment.getAccount().getName())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    public KanbanTaskMemberDto convertToDto(KanbanTaskMember member) {
        return KanbanTaskMemberDto.builder()
                .id(member.getId())
                .kanbanTaskId(member.getKanbanTask().getId())
                .accountId(member.getAccount().getId())
                .memberName(member.getAccount().getName())
                .profileImage(member.getAccount().getProfileImageUrl())
                .assignedAt(member.getCreatedAt())
                .build();
    }

    public KanbanTaskAttachDto convertToDto(KanbanTaskAttach attach) {
        return KanbanTaskAttachDto.builder()
                .id(attach.getId())
                .kanbanTaskId(attach.getKanbanTask().getId())
                .fileInfoId(attach.getFileInfo().getId())
                .fileName(attach.getFileInfo().getNameOrigin())
                .fileUrl("/files/" + attach.getFileInfo().getNameHashed())
                .fileType("application/octet-stream")
                .fileSize(attach.getFileInfo().getSize())
                .uploadedAt(attach.getCreatedAt())
                .uploaderName("User")
                .build();
    }

    // 댓글 관련 메서드들
    @Transactional
    public KanbanTaskCommentDto updateComment(Long commentId, KanbanTaskCommentUpdateRequest request, Long userId) {
        KanbanTaskComment comment = kanbanTaskCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        // 댓글 작성자 확인
        if (!comment.getAccount().getId().equals(userId)) {
            throw new RuntimeException("댓글 수정 권한이 없습니다.");
        }

        comment.setComment(request.getComment());
        comment = kanbanTaskCommentRepository.save(comment);
        return convertToDto(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        KanbanTaskComment comment = kanbanTaskCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        // 댓글 작성자 확인 (실제 구현에서는 권한 체크 로직 추가 필요)
        if (!comment.getAccount().getId().equals(userId)) {
            throw new RuntimeException("댓글 삭제 권한이 없습니다.");
        }

        comment.markDeleted();
        kanbanTaskCommentRepository.save(comment);
    }

    public Page<KanbanTaskCommentDto> getCommentsByTaskId(Long taskId, Pageable pageable) {
        Page<KanbanTaskComment> comments = kanbanTaskCommentRepository.findByKanbanTaskIdPageable(taskId, pageable);
        return comments.map(this::convertToDto);
    }

    // 칸반 리스트 관련 메서드들
    @Transactional
    public KanbanListDto updateKanbanList(Long listId, KanbanListUpdateRequest request) {
        KanbanList kanbanList = kanbanListRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("칸반 리스트를 찾을 수 없습니다."));

        if (request.getKanbanListName() != null) {
            kanbanList.setKanbanListName(request.getKanbanListName());
        }
        if (request.getOrder() != null) {
            kanbanList.setOrder(request.getOrder());
        }

        kanbanList = kanbanListRepository.save(kanbanList);
        return convertToDto(kanbanList);
    }

    @Transactional
    public void deleteKanbanList(Long listId) {
        KanbanList kanbanList = kanbanListRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("칸반 리스트를 찾을 수 없습니다."));

        kanbanList.markDeleted();
        kanbanListRepository.save(kanbanList);
    }
    
    // 작업 완료 요청/승인 관련 메서드들
    @Transactional
    public KanbanTaskDto requestTaskCompletion(Long cardId, KanbanTaskCompletionRequest request) {
        KanbanTask kanbanTask = kanbanTaskRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("칸반 태스크를 찾을 수 없습니다."));
        
        kanbanTask.setCompletionRequested(true);
        kanbanTask.setCompletionRequestMessage(request.getMessage());
        
        kanbanTask = kanbanTaskRepository.save(kanbanTask);
        return convertToDto(kanbanTask);
    }
    
    @Transactional
    public KanbanTaskDto approveTaskCompletion(Long cardId, KanbanTaskCompletionApprovalRequest request) {
        KanbanTask kanbanTask = kanbanTaskRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("칸반 태스크를 찾을 수 없습니다."));
        
        if (request.getApproved()) {
            kanbanTask.setIsApproved(true);
            kanbanTask.setCompletionRequested(false);
        } else {
            kanbanTask.setCompletionRequested(false);
        }
        
        kanbanTask = kanbanTaskRepository.save(kanbanTask);
        return convertToDto(kanbanTask);
    }
}