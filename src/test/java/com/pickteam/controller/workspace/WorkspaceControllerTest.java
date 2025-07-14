package com.pickteam.controller.workspace;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pickteam.controller.WorkspaceController;
import com.pickteam.dto.workspace.*;
import com.pickteam.dto.user.UserSummaryResponse;
import com.pickteam.service.WorkspaceService;
import com.pickteam.security.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 워크스페이스 컨트롤러 단위 테스트
 */
@WebMvcTest(
        value = WorkspaceController.class,
        useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = WorkspaceController.class
        ),
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
        }
)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.datasource.initialization-mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@ActiveProfiles("test")
class WorkspaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkspaceService workspaceService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 테스트를 위한 Security Context 설정 헬퍼 메서드
     */
    private void setUpSecurityContext(Long userId) {
        UserPrincipal userPrincipal = new UserPrincipal(
                userId,
                "test@example.com",
                "테스트 사용자",
                "password",
                com.pickteam.domain.enums.UserRole.USER,
                java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
        );
        
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("워크스페이스 생성 시 정상적인 요청이면 200 OK를 반환한다")
    void createWorkspace_ValidRequest_Returns201Created() throws Exception {
        // given
        setUpSecurityContext(1L);
        
        WorkspaceCreateRequest request = new WorkspaceCreateRequest();
        request.setName("새로운 워크스페이스");
        request.setIconUrl("🏢");
        request.setPassword("password123");

        WorkspaceResponse expectedResponse = WorkspaceResponse.builder()
                .id(1L)
                .name("새로운 워크스페이스")
                .iconUrl("🏢")
                .inviteCode("ABC123")
                .passwordProtected(true)
                .memberCount(1)
                .createdAt(LocalDateTime.now())
                .build();

        given(workspaceService.createWorkspace(anyLong(), any(WorkspaceCreateRequest.class)))
                .willReturn(expectedResponse);

        // when & then
        mockMvc.perform(post("/api/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("새로운 워크스페이스"));

        verify(workspaceService).createWorkspace(anyLong(), any(WorkspaceCreateRequest.class));
    }

    @Test
    @DisplayName("워크스페이스 생성 시 이름이 비어있으면 400 Bad Request를 반환한다")
    void createWorkspace_EmptyName_Returns400BadRequest() throws Exception {
        // given
        setUpSecurityContext(1L);
        
        WorkspaceCreateRequest request = new WorkspaceCreateRequest();
        request.setName("");
        request.setPassword("password123");

        // when & then
        mockMvc.perform(post("/api/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("초대 링크로 워크스페이스 참여 시 정상적인 요청이면 200 OK를 반환한다")
    void joinWorkspace_ValidRequest_Returns200OK() throws Exception {
        // given
        setUpSecurityContext(1L);
        
        WorkspaceJoinRequest request = new WorkspaceJoinRequest();
        request.setInviteCode("ABC123");
        request.setPassword("password123");

        WorkspaceResponse expectedResponse = WorkspaceResponse.builder()
                .id(1L)
                .name("참여할 워크스페이스")
                .inviteCode("ABC123")
                .passwordProtected(true)
                .memberCount(2)
                .build();

        given(workspaceService.joinWorkspace(anyLong(), any(WorkspaceJoinRequest.class)))
                .willReturn(expectedResponse);

        // when & then
        mockMvc.perform(post("/api/workspaces/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L));

        verify(workspaceService).joinWorkspace(anyLong(), any(WorkspaceJoinRequest.class));
    }

    @Test
    @DisplayName("사용자가 속한 워크스페이스 목록 조회 시 정상적으로 목록을 반환한다")
    void getUserWorkspaces_ValidRequest_Returns200OK() throws Exception {
        // given
        setUpSecurityContext(1L);
        
        List<WorkspaceResponse> expectedWorkspaces = List.of(
                WorkspaceResponse.builder().id(1L).name("워크스페이스 1").build(),
                WorkspaceResponse.builder().id(2L).name("워크스페이스 2").build()
        );

        given(workspaceService.getUserWorkspaces(anyLong()))
                .willReturn(expectedWorkspaces);

        // when & then
        mockMvc.perform(get("/api/workspaces/my"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(workspaceService).getUserWorkspaces(anyLong());
    }

    @Test
    @DisplayName("워크스페이스 상세 조회 시 정상적으로 워크스페이스 정보를 반환한다")
    void getWorkspace_ValidRequest_Returns200OK() throws Exception {
        // given
        setUpSecurityContext(1L);
        
        Long workspaceId = 1L;
        WorkspaceResponse expectedResponse = WorkspaceResponse.builder()
                .id(workspaceId)
                .name("상세 워크스페이스")
                .memberCount(5)
                .build();

        given(workspaceService.getWorkspace(eq(workspaceId), anyLong()))
                .willReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/api/workspaces/{workspaceId}", workspaceId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(workspaceId));

        verify(workspaceService).getWorkspace(eq(workspaceId), anyLong());
    }

    @Test
    @DisplayName("워크스페이스 멤버 목록 조회 시 정상적으로 멤버 목록을 반환한다")
    void getWorkspaceMembers_ValidRequest_Returns200OK() throws Exception {
        // given
        setUpSecurityContext(1L);
        
        Long workspaceId = 1L;
        List<UserSummaryResponse> expectedMembers = List.of(
                UserSummaryResponse.builder().id(1L).name("사용자1").build(),
                UserSummaryResponse.builder().id(2L).name("사용자2").build()
        );

        given(workspaceService.getWorkspaceMembers(eq(workspaceId), anyLong()))
                .willReturn(expectedMembers);

        // when & then
        mockMvc.perform(get("/api/workspaces/{workspaceId}/members", workspaceId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(workspaceService).getWorkspaceMembers(eq(workspaceId), anyLong());
    }
}
