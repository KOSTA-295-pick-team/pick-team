/** 단위 테스트 재작성 예정
 */

//package com.pickteam.service.kanban;
//
//import com.pickteam.domain.kanban.*;
//import com.pickteam.domain.team.Team;
//import com.pickteam.domain.workspace.Workspace;
//import com.pickteam.dto.kanban.*;
//import com.pickteam.repository.kanban.*;
//import com.pickteam.repository.team.TeamRepository;
//import com.pickteam.repository.user.AccountRepository;
//import com.pickteam.repository.workspace.WorkspaceRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDateTime;
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.verify;
//
///**
// * 칸반 서비스 단위 테스트
// * Business Layer 로직 검증에 집중
// */
//@ExtendWith(MockitoExtension.class)
//class KanbanServiceTest {
//
//    @Mock
//    private KanbanRepository kanbanRepository;
//
//    @Mock
//    private KanbanListRepository kanbanListRepository;
//
//    @Mock
//    private KanbanTaskRepository kanbanTaskRepository;
//
//    @Mock
//    private KanbanTaskCommentRepository kanbanTaskCommentRepository;
//
//    @Mock
//    private KanbanTaskMemberRepository kanbanTaskMemberRepository;
//
//    @Mock
//    private TeamRepository teamRepository;
//
//    @Mock
//    private WorkspaceRepository workspaceRepository;
//
//    @Mock
//    private AccountRepository accountRepository;
//
//    @Mock
//    private KanbanServiceHelper helper;
//
//    @InjectMocks
//    private KanbanService kanbanService;
//
//    private Team mockTeam;
//    private Workspace mockWorkspace;
//
//    @BeforeEach
//    void setUp() {
//        mockTeam = Team.builder()
//                .id(1L)
//                .name("테스트 팀")
//                .build();
//
//        mockWorkspace = Workspace.builder()
//                .id(1L)
//                .name("테스트 워크스페이스")
//                .build();
//    }
//
//    @Test
//    @DisplayName("칸반을 생성할 수 있다")
//    void createKanban_ValidRequest_ReturnsKanbanDto() {
//        // Given
//        KanbanCreateRequest request = KanbanCreateRequest.builder()
//                .teamId(1L)
//                .workspaceId(1L)
//                .build();
//
//        Kanban kanban = Kanban.builder()
//                .id(1L)
//                .team(mockTeam)
//                .workspace(mockWorkspace)
//                .build();
//
//        KanbanDto expectedDto = KanbanDto.builder()
//                .id(1L)
//                .teamId(1L)
//                .workspaceId(1L)
//                .kanbanLists(Collections.emptyList())
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        given(teamRepository.findById(anyLong())).willReturn(Optional.of(mockTeam));
//        given(workspaceRepository.findById(anyLong())).willReturn(Optional.of(mockWorkspace));
//        given(kanbanRepository.save(any(Kanban.class))).willReturn(kanban);
//        given(helper.convertToDto(kanban)).willReturn(expectedDto);
//
//        // When
//        KanbanDto result = kanbanService.createKanban(request);
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isEqualTo(1L);
//        assertThat(result.getTeamId()).isEqualTo(1L);
//        assertThat(result.getWorkspaceId()).isEqualTo(1L);
//
//        verify(teamRepository).findById(1L);
//        verify(workspaceRepository).findById(1L);
//        verify(kanbanRepository).save(any(Kanban.class));
//        verify(helper).createDefaultLists(any(Kanban.class));
//        verify(helper).convertToDto(kanban);
//    }
//
//    @Test
//    @DisplayName("팀 ID로 칸반을 조회할 수 있다")
//    void getKanbanByTeamId_ValidTeamId_ReturnsKanbanDto() {
//        // Given
//        Long teamId = 1L;
//
//        Kanban kanban = Kanban.builder()
//                .id(1L)
//                .team(mockTeam)
//                .workspace(mockWorkspace)
//                .build();
//
//        KanbanDto expectedDto = KanbanDto.builder()
//                .id(1L)
//                .teamId(teamId)
//                .workspaceId(1L)
//                .kanbanLists(Collections.emptyList())
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        given(kanbanRepository.findByTeamId(teamId)).willReturn(Optional.of(kanban));
//        given(helper.convertToDto(kanban)).willReturn(expectedDto);
//
//        // When
//        KanbanDto result = kanbanService.getKanbanByTeamId(teamId);
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isEqualTo(1L);
//        assertThat(result.getTeamId()).isEqualTo(teamId);
//
//        verify(kanbanRepository).findByTeamId(teamId);
//        verify(helper).convertToDto(kanban);
//    }
//
//    @Test
//    @DisplayName("칸반 리스트를 생성할 수 있다")
//    void createKanbanList_ValidRequest_ReturnsKanbanListDto() {
//        // Given
//        KanbanListCreateRequest request = KanbanListCreateRequest.builder()
//                .kanbanId(1L)
//                .kanbanListName("새로운 리스트")
//                .build();
//
//        Kanban kanban = Kanban.builder()
//                .id(1L)
//                .team(mockTeam)
//                .workspace(mockWorkspace)
//                .build();
//
//        KanbanList kanbanList = KanbanList.builder()
//                .id(1L)
//                .kanbanListName("새로운 리스트")
//                .kanban(kanban)
//                .order(0)
//                .build();
//
//        KanbanListDto expectedDto = KanbanListDto.builder()
//                .id(1L)
//                .kanbanListName("새로운 리스트")
//                .kanbanId(1L)
//                .order(0)
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        given(kanbanRepository.findById(anyLong())).willReturn(Optional.of(kanban));
//        given(kanbanListRepository.findMaxOrderByKanbanId(anyLong())).willReturn(null);
//        given(kanbanListRepository.save(any(KanbanList.class))).willReturn(kanbanList);
//        given(helper.convertToDto(kanbanList)).willReturn(expectedDto);
//
//        // When
//        KanbanListDto result = kanbanService.createKanbanList(request);
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isEqualTo(1L);
//        assertThat(result.getKanbanListName()).isEqualTo("새로운 리스트");
//        assertThat(result.getKanbanId()).isEqualTo(1L);
//
//        verify(kanbanRepository).findById(1L);
//        verify(kanbanListRepository).findMaxOrderByKanbanId(1L);
//        verify(kanbanListRepository).save(any(KanbanList.class));
//        verify(helper).convertToDto(kanbanList);
//    }
//
//    @Test
//    @DisplayName("칸반 태스크를 생성할 수 있다")
//    void createKanbanTask_ValidRequest_ReturnsKanbanTaskDto() {
//        // Given
//        KanbanTaskCreateRequest request = KanbanTaskCreateRequest.builder()
//                .subject("새로운 태스크")
//                .content("태스크 내용")
//                .kanbanListId(1L)
//                .build();
//
//        KanbanTaskDto expectedDto = KanbanTaskDto.builder()
//                .id(1L)
//                .subject("새로운 태스크")
//                .content("태스크 내용")
//                .kanbanListId(1L)
//                .order(0)
//                .isApproved(false)
//                .comments(Collections.emptyList())
//                .members(Collections.emptyList())
//                .attachments(Collections.emptyList())
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        KanbanList kanbanList = KanbanList.builder()
//                .id(1L)
//                .kanbanListName("새로운 태스크")
//                .order(0)
//                .build();
//
//        KanbanTask kanbanTask = KanbanTask.builder()
//                .id(1L)
//                .subject("새로운 태스크")
//                .content("태스크 내용")
//                .kanbanList(kanbanList)
//                .order(0)
//                .isApproved(false)
//                .build();
//
//        given(kanbanListRepository.findById(anyLong())).willReturn(Optional.of(kanbanList));
//        given(kanbanTaskRepository.findMaxOrderByKanbanListId(anyLong())).willReturn(null);
//        given(kanbanTaskRepository.save(any(KanbanTask.class))).willReturn(kanbanTask);
//        given(helper.convertToDto(kanbanTask)).willReturn(expectedDto);
//
//        // When
//        KanbanTaskDto result = kanbanService.createKanbanTask(request);
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isEqualTo(1L);
//        assertThat(result.getSubject()).isEqualTo("새로운 태스크");
//        assertThat(result.getContent()).isEqualTo("태스크 내용");
//        assertThat(result.getKanbanListId()).isEqualTo(1L);
//        assertThat(result.getOrder()).isEqualTo(0);
//        assertThat(result.getIsApproved()).isFalse();
//
//        verify(kanbanListRepository).findById(1L);
//        verify(kanbanTaskRepository).findMaxOrderByKanbanListId(1L);
//        verify(kanbanTaskRepository).save(any(KanbanTask.class));
//        verify(helper).convertToDto(kanbanTask);
//    }
//
//    @Test
//    @DisplayName("칸반 태스크를 수정할 수 있다")
//    void updateKanbanTask_ValidRequest_ReturnsUpdatedKanbanTaskDto() {
//        // Given
//        Long taskId = 1L;
//        KanbanTaskUpdateRequest request = KanbanTaskUpdateRequest.builder()
//                .subject("수정된 태스크")
//                .content("수정된 내용")
//                .isApproved(true)
//                .build();
//
//        KanbanTaskDto expectedDto = KanbanTaskDto.builder()
//                .id(taskId)
//                .subject("수정된 태스크")
//                .content("수정된 내용")
//                .kanbanListId(1L)
//                .order(0)
//                .isApproved(true)
//                .comments(Collections.emptyList())
//                .members(Collections.emptyList())
//                .attachments(Collections.emptyList())
//                .updatedAt(LocalDateTime.now())
//                .build();
//
//        given(helper.updateKanbanTask(taskId, request)).willReturn(expectedDto);
//
//        // When
//        KanbanTaskDto result = kanbanService.updateKanbanTask(taskId, request);
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isEqualTo(taskId);
//        assertThat(result.getSubject()).isEqualTo("수정된 태스크");
//        assertThat(result.getContent()).isEqualTo("수정된 내용");
//        assertThat(result.getIsApproved()).isTrue();
//
//        verify(helper).updateKanbanTask(taskId, request);
//    }
//
//    @Test
//    @DisplayName("칸반 태스크 댓글을 생성할 수 있다")
//    void createComment_ValidRequest_ReturnsKanbanTaskCommentDto() {
//        // Given
//        KanbanTaskCommentCreateRequest request = KanbanTaskCommentCreateRequest.builder()
//                .kanbanTaskId(1L)
//                .comment("새 댓글입니다")
//                .build();
//
//        Long authorId = 1L;
//
//        KanbanTaskCommentDto expectedDto = KanbanTaskCommentDto.builder()
//                .id(1L)
//                .comment("새 댓글입니다")
//                .kanbanTaskId(1L)
//                .accountId(authorId)
//                .authorName("테스트 사용자")
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        given(helper.createComment(request, authorId)).willReturn(expectedDto);
//
//        // When
//        KanbanTaskCommentDto result = kanbanService.createComment(request, authorId);
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isEqualTo(1L);
//        assertThat(result.getComment()).isEqualTo("새 댓글입니다");
//        assertThat(result.getKanbanTaskId()).isEqualTo(1L);
//        assertThat(result.getAccountId()).isEqualTo(authorId);
//
//        verify(helper).createComment(request, authorId);
//    }
//
//    @Test
//    @DisplayName("칸반 태스크를 삭제할 수 있다")
//    void deleteKanbanTask_ValidTaskId_DeletesSuccessfully() {
//        // Given
//        Long taskId = 1L;
//
//        // When
//        kanbanService.deleteKanbanTask(taskId);
//
//        // Then
//        verify(helper).deleteKanbanTask(taskId);
//    }
//
//    @Test
//    @DisplayName("칸반 태스크를 조회할 수 있다")
//    void getKanbanTask_ValidTaskId_ReturnsKanbanTaskDto() {
//        // Given
//        Long taskId = 1L;
//
//        KanbanTask kanbanTask = KanbanTask.builder()
//                .id(taskId)
//                .subject("테스트 태스크")
//                .content("태스크 내용")
//                .order(0)
//                .isApproved(false)
//                .build();
//
//        KanbanTaskDto expectedDto = KanbanTaskDto.builder()
//                .id(taskId)
//                .subject("테스트 태스크")
//                .content("태스크 내용")
//                .order(0)
//                .isApproved(false)
//                .comments(Collections.emptyList())
//                .members(Collections.emptyList())
//                .attachments(Collections.emptyList())
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        given(kanbanTaskRepository.findById(taskId)).willReturn(Optional.of(kanbanTask));
//        given(helper.convertToDto(kanbanTask)).willReturn(expectedDto);
//
//        // When
//        KanbanTaskDto result = kanbanService.getKanbanTask(taskId);
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isEqualTo(taskId);
//        assertThat(result.getSubject()).isEqualTo("테스트 태스크");
//        assertThat(result.getContent()).isEqualTo("태스크 내용");
//
//        verify(kanbanTaskRepository).findById(taskId);
//        verify(helper).convertToDto(kanbanTask);
//    }
//
//    @Test
//    @DisplayName("리스트 ID로 태스크 목록을 조회할 수 있다")
//    void getTasksByListId_ValidListId_ReturnsKanbanTaskDtoList() {
//        // Given
//        Long listId = 1L;
//
//        KanbanTask kanbanTask1 = KanbanTask.builder()
//                .id(1L)
//                .subject("태스크 1")
//                .order(0)
//                .build();
//
//        KanbanTask kanbanTask2 = KanbanTask.builder()
//                .id(2L)
//                .subject("태스크 2")
//                .order(1)
//                .build();
//
//        List<KanbanTask> kanbanTasks = List.of(kanbanTask1, kanbanTask2);
//
//        KanbanTaskDto taskDto1 = KanbanTaskDto.builder()
//                .id(1L)
//                .subject("태스크 1")
//                .order(0)
//                .build();
//
//        KanbanTaskDto taskDto2 = KanbanTaskDto.builder()
//                .id(2L)
//                .subject("태스크 2")
//                .order(1)
//                .build();
//
//        given(kanbanTaskRepository.findByKanbanListIdOrderByOrder(listId)).willReturn(kanbanTasks);
//        given(helper.convertToDto(kanbanTask1)).willReturn(taskDto1);
//        given(helper.convertToDto(kanbanTask2)).willReturn(taskDto2);
//
//        // When
//        List<KanbanTaskDto> result = kanbanService.getTasksByListId(listId);
//
//        // Then
//        assertThat(result).hasSize(2);
//        assertThat(result.get(0).getId()).isEqualTo(1L);
//        assertThat(result.get(0).getSubject()).isEqualTo("태스크 1");
//        assertThat(result.get(1).getId()).isEqualTo(2L);
//        assertThat(result.get(1).getSubject()).isEqualTo("태스크 2");
//
//        verify(kanbanTaskRepository).findByKanbanListIdOrderByOrder(listId);
//        verify(helper).convertToDto(kanbanTask1);
//        verify(helper).convertToDto(kanbanTask2);
//    }
//}