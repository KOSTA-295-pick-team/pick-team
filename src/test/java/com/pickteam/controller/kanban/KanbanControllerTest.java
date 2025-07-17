/**
 * 단위 테스트 재작성 예정
 */

//package com.pickteam.controller.kanban;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.pickteam.config.TestSecurityConfig;
//import com.pickteam.dto.kanban.*;
//import com.pickteam.exception.GlobalExceptionHandler;
//import com.pickteam.service.kanban.KanbanService;
//import com.pickteam.security.UserPrincipal;
//import com.pickteam.domain.enums.UserRole;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.FilterType;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.doNothing;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
///**
// * 칸반 컨트롤러 단위 테스트
// * @WebMvcTest를 사용하여 Presentation Layer만 테스트
// * Security 설정은 제외하고 Controller 로직만 검증
// */
//@WebMvcTest(
//        value = KanbanController.class,
//        useDefaultFilters = false,
//        includeFilters = @ComponentScan.Filter(
//                type = FilterType.ASSIGNABLE_TYPE,
//                classes = KanbanController.class
//        ),
//        excludeAutoConfiguration = {
//                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
//                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
//                org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
//        }
//)
//@TestPropertySource(properties = {
//        "spring.jpa.hibernate.ddl-auto=none",
//        "spring.datasource.initialization-mode=never",
//        "spring.jpa.defer-datasource-initialization=false"
//})
//@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
//@ActiveProfiles("test")
//class KanbanControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockitoBean
//    private KanbanService kanbanService;
//
//    @Test
//    @DisplayName("칸반 보드를 생성할 수 있다")
//    void createKanban_ValidRequest_ReturnsKanbanDto() throws Exception {
//        // Given
//        KanbanCreateRequest request = KanbanCreateRequest.builder()
//                .teamId(1L)
//                .workspaceId(1L)
//                .build();
//
//        KanbanDto response = KanbanDto.builder()
//                .id(1L)
//                .teamId(1L)
//                .workspaceId(1L)
//                .kanbanLists(List.of())
//                .createdAt(LocalDateTime.now())
//                .updatedAt(LocalDateTime.now())
//                .build();
//
//        given(kanbanService.createKanban(any(KanbanCreateRequest.class)))
//                .willReturn(response);
//
//        // When & Then
//        mockMvc.perform(post("/api/kanban")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.message").value("칸반 보드가 생성되었습니다."))
//                .andExpect(jsonPath("$.data.id").value(1L))
//                .andExpect(jsonPath("$.data.teamId").value(1L))
//                .andExpect(jsonPath("$.data.workspaceId").value(1L));
//
//        verify(kanbanService).createKanban(any(KanbanCreateRequest.class));
//    }
//
//    @Test
//    @DisplayName("팀 ID로 칸반 보드를 조회할 수 있다")
//    void getKanbanByTeamId_ValidTeamId_ReturnsKanbanDto() throws Exception {
//        // Given
//        Long teamId = 1L;
//        KanbanDto response = KanbanDto.builder()
//                .id(1L)
//                .teamId(teamId)
//                .workspaceId(1L)
//                .kanbanLists(List.of())
//                .createdAt(LocalDateTime.now())
//                .updatedAt(LocalDateTime.now())
//                .build();
//
//        given(kanbanService.getKanbanByTeamId(teamId))
//                .willReturn(response);
//
//        // When & Then
//        mockMvc.perform(get("/api/kanban/team/{teamId}", teamId))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.message").value("칸반 보드를 조회했습니다."))
//                .andExpect(jsonPath("$.data.id").value(1L))
//                .andExpect(jsonPath("$.data.teamId").value(teamId));
//
//        verify(kanbanService).getKanbanByTeamId(teamId);
//    }
//
//    @Test
//    @DisplayName("칸반 태스크를 생성할 수 있다")
//    void createTask_ValidRequest_ReturnsKanbanTaskDto() throws Exception {
//        // Given
//        KanbanTaskCreateRequest request = KanbanTaskCreateRequest.builder()
//                .subject("새 태스크")
//                .content("태스크 내용")
//                .kanbanListId(1L)
//                .deadline(LocalDateTime.now().plusDays(7))
//                .assigneeIds(List.of(1L, 2L))
//                .build();
//
//        KanbanTaskDto response = KanbanTaskDto.builder()
//                .id(1L)
//                .subject("새 태스크")
//                .content("태스크 내용")
//                .kanbanListId(1L)
//                .order(0)
//                .isApproved(false)
//                .comments(List.of())
//                .members(List.of())
//                .attachments(List.of())
//                .createdAt(LocalDateTime.now())
//                .updatedAt(LocalDateTime.now())
//                .build();
//
//        given(kanbanService.createKanbanTask(any(KanbanTaskCreateRequest.class)))
//                .willReturn(response);
//
//        // When & Then
//        mockMvc.perform(post("/api/kanban/tasks")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.message").value("칸반 태스크가 생성되었습니다."))
//                .andExpect(jsonPath("$.data.id").value(1L))
//                .andExpect(jsonPath("$.data.subject").value("새 태스크"))
//                .andExpect(jsonPath("$.data.content").value("태스크 내용"))
//                .andExpect(jsonPath("$.data.isApproved").value(false));
//
//        verify(kanbanService).createKanbanTask(any(KanbanTaskCreateRequest.class));
//    }
//
//    @Test
//    @DisplayName("칸반 태스크를 수정할 수 있다")
//    void updateTask_ValidRequest_ReturnsUpdatedKanbanTaskDto() throws Exception {
//        // Given
//        Long taskId = 1L;
//        KanbanTaskUpdateRequest request = KanbanTaskUpdateRequest.builder()
//                .subject("수정된 태스크")
//                .content("수정된 내용")
//                .isApproved(true)
//                .build();
//
//        KanbanTaskDto response = KanbanTaskDto.builder()
//                .id(taskId)
//                .subject("수정된 태스크")
//                .content("수정된 내용")
//                .kanbanListId(1L)
//                .order(0)
//                .isApproved(true)
//                .comments(List.of())
//                .members(List.of())
//                .attachments(List.of())
//                .createdAt(LocalDateTime.now())
//                .updatedAt(LocalDateTime.now())
//                .build();
//
//        given(kanbanService.updateKanbanTask(eq(taskId), any(KanbanTaskUpdateRequest.class)))
//                .willReturn(response);
//
//        // When & Then
//        mockMvc.perform(put("/api/kanban/tasks/{taskId}", taskId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.message").value("칸반 태스크가 수정되었습니다."))
//                .andExpect(jsonPath("$.data.id").value(taskId))
//                .andExpect(jsonPath("$.data.subject").value("수정된 태스크"))
//                .andExpect(jsonPath("$.data.isApproved").value(true));
//
//        verify(kanbanService).updateKanbanTask(eq(taskId), any(KanbanTaskUpdateRequest.class));
//    }
//
//    @Test
//    @DisplayName("칸반 태스크를 삭제할 수 있다")
//    void deleteTask_ValidTaskId_ReturnsSuccessMessage() throws Exception {
//        // Given
//        Long taskId = 1L;
//        doNothing().when(kanbanService).deleteKanbanTask(taskId);
//
//        // When & Then
//        mockMvc.perform(delete("/api/kanban/tasks/{taskId}", taskId))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.message").value("칸반 태스크가 삭제되었습니다."))
//                .andExpect(jsonPath("$.data").isEmpty());
//
//        verify(kanbanService).deleteKanbanTask(taskId);
//    }
//
//    @Test
//    @DisplayName("칸반 태스크에 댓글을 추가할 수 있다")
//    void createComment_ValidRequest_ReturnsKanbanTaskCommentDto() throws Exception {
//        // Given
//        Long taskId = 1L;
//        Long accountId = 1L;
//
//        UserPrincipal userPrincipal = new UserPrincipal(
//                accountId,
//                "test@example.com",
//                "테스트 사용자",
//                "password",
//                UserRole.USER,
//                List.of()
//        );
//
//        KanbanTaskCommentCreateRequest request = KanbanTaskCommentCreateRequest.builder()
//                .comment("새 댓글입니다")
//                .kanbanTaskId(taskId)
//                .build();
//
//        KanbanTaskCommentDto response = KanbanTaskCommentDto.builder()
//                .id(1L)
//                .comment("새 댓글입니다")
//                .kanbanTaskId(taskId)
//                .accountId(accountId)
//                .authorName("테스트 사용자")
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        given(kanbanService.createComment(any(KanbanTaskCommentCreateRequest.class), eq(accountId)))
//                .willReturn(response);
//
//        // When & Then
//        mockMvc.perform(post("/api/kanban/tasks/{taskId}/comments", taskId)
//                        .with(user(userPrincipal))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.message").value("댓글이 추가되었습니다."))
//                .andExpect(jsonPath("$.data.id").value(1L))
//                .andExpect(jsonPath("$.data.comment").value("새 댓글입니다"))
//                .andExpect(jsonPath("$.data.kanbanTaskId").value(taskId));
//
//        verify(kanbanService).createComment(any(KanbanTaskCommentCreateRequest.class), eq(accountId));
//    }
//}
