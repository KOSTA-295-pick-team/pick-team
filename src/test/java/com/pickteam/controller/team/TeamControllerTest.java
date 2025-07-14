package com.pickteam.controller.team;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pickteam.config.TestSecurityConfig;
import com.pickteam.domain.enums.UserRole;
import com.pickteam.domain.team.TeamMember;
import com.pickteam.dto.team.TeamCreateRequest;
import com.pickteam.dto.team.TeamMemberResponse;
import com.pickteam.dto.team.TeamResponse;
import com.pickteam.dto.team.TeamUpdateRequest;
import com.pickteam.dto.user.UserSummaryResponse;
import com.pickteam.exception.GlobalExceptionHandler;
import com.pickteam.security.UserPrincipal;
import com.pickteam.service.team.TeamService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 팀 컨트롤러 단위 테스트
 * @WebMvcTest를 사용하여 Presentation Layer만 테스트
 * TestSecurityConfig와 Mock 인증을 사용하여 완전한 HTTP 테스트 수행
 */
@WebMvcTest(
        value = TeamController.class,
        useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = TeamController.class
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
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TeamService teamService;

    /**
     * 테스트용 Mock 사용자 생성
     */
    private UserPrincipal createTestUser() {
        return new UserPrincipal(
                1L,
                "test@example.com",
                "테스트 사용자",
                "password",
                UserRole.USER,
                List.of()
        );
    }

    @Test
    @DisplayName("워크스페이스별 팀 목록을 조회할 수 있다")
    void getTeamsByWorkspace_ValidWorkspaceId_ReturnsTeamsList() throws Exception {
        // Given
        Long workspaceId = 1L;
        TeamResponse teamResponse = createTeamResponse();

        given(teamService.getTeamsByWorkspace(eq(workspaceId), eq(1L)))
                .willReturn(List.of(teamResponse));

        // When & Then
        mockMvc.perform(get("/api/teams/workspace/{workspaceId}", workspaceId)
                        .with(user(createTestUser())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("팀 목록 조회 성공"))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].name").value("테스트 팀"))
                .andExpect(jsonPath("$.data[0].workspaceId").value(1L));

        verify(teamService).getTeamsByWorkspace(eq(workspaceId), eq(1L));
    }

    @Test
    @DisplayName("팀 상세 정보를 조회할 수 있다")
    void getTeam_ValidTeamId_ReturnsTeamDetails() throws Exception {
        // Given
        Long teamId = 1L;
        TeamResponse teamResponse = createTeamResponse();

        given(teamService.getTeam(eq(teamId), eq(1L)))
                .willReturn(teamResponse);

        // When & Then
        mockMvc.perform(get("/api/teams/{teamId}", teamId)
                        .with(user(createTestUser())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("팀 조회 성공"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("테스트 팀"))
                .andExpect(jsonPath("$.data.memberCount").value(1));

        verify(teamService).getTeam(eq(teamId), eq(1L));
    }

    @Test
    @DisplayName("팀 멤버 목록을 조회할 수 있다")
    void getTeamMembers_ValidTeamId_ReturnsMembersList() throws Exception {
        // Given
        Long teamId = 1L;
        TeamMemberResponse memberResponse = createTeamMemberResponse();

        given(teamService.getTeamMembers(eq(teamId), eq(1L)))
                .willReturn(List.of(memberResponse));

        // When & Then
        mockMvc.perform(get("/api/teams/{teamId}/members", teamId)
                        .with(user(createTestUser())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("팀 멤버 목록 조회 성공"))
                .andExpect(jsonPath("$.data[0].accountId").value(1L))
                .andExpect(jsonPath("$.data[0].name").value("테스트 사용자"));

        verify(teamService).getTeamMembers(eq(teamId), eq(1L));
    }

    @Test
    @DisplayName("팀을 생성할 수 있다")
    void createTeam_ValidRequest_ReturnsTeamResponse() throws Exception {
        // Given
        TeamCreateRequest request = new TeamCreateRequest();
        request.setName("새 팀");
        request.setWorkspaceId(1L);

        TeamResponse response = createTeamResponse();
        response.setName("새 팀");

        given(teamService.createTeam(eq(1L), any(TeamCreateRequest.class)))
                .willReturn(response);

        // When & Then
        mockMvc.perform(post("/api/teams")
                        .with(user(createTestUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("팀 생성 성공"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("새 팀"))
                .andExpect(jsonPath("$.data.workspaceId").value(1L));

        verify(teamService).createTeam(eq(1L), any(TeamCreateRequest.class));
    }

    @Test
    @DisplayName("팀을 수정할 수 있다")
    void updateTeam_ValidRequest_ReturnsUpdatedTeam() throws Exception {
        // Given
        Long teamId = 1L;
        TeamUpdateRequest request = new TeamUpdateRequest();
        request.setName("수정된 팀명");

        TeamResponse response = createTeamResponse();
        response.setName("수정된 팀명");

        given(teamService.updateTeam(eq(teamId), eq(1L), any(TeamUpdateRequest.class)))
                .willReturn(response);

        // When & Then
        mockMvc.perform(put("/api/teams/{teamId}", teamId)
                        .with(user(createTestUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("팀 수정 성공"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("수정된 팀명"));

        verify(teamService).updateTeam(eq(teamId), eq(1L), any(TeamUpdateRequest.class));
    }

    @Test
    @DisplayName("팀을 삭제할 수 있다")
    void deleteTeam_ValidTeamId_ReturnsSuccessMessage() throws Exception {
        // Given
        Long teamId = 1L;
        doNothing().when(teamService).deleteTeam(teamId, 1L);

        // When & Then
        mockMvc.perform(delete("/api/teams/{teamId}", teamId)
                        .with(user(createTestUser())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("팀 삭제 성공"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(teamService).deleteTeam(teamId, 1L);
    }

    @Test
    @DisplayName("Security가 비활성화된 상태에서 서비스 계층 Mock 검증")
    void verifyServiceLayerMocking() {
        // Given
        Long workspaceId = 1L;
        Long accountId = 1L;
        TeamResponse teamResponse = createTeamResponse();

        given(teamService.getTeamsByWorkspace(workspaceId, accountId))
                .willReturn(List.of(teamResponse));

        // When
        List<TeamResponse> result = teamService.getTeamsByWorkspace(workspaceId, accountId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("테스트 팀");
        verify(teamService).getTeamsByWorkspace(workspaceId, accountId);
    }

    @Test
    @DisplayName("팀 생성 요청 JSON 검증")
    void validateTeamCreateRequestJson() throws Exception {
        // Given
        TeamCreateRequest request = new TeamCreateRequest();
        request.setName("새 팀");
        request.setWorkspaceId(1L);

        // When & Then
        String jsonContent = objectMapper.writeValueAsString(request);
        assertThat(jsonContent).contains("새 팀");
        assertThat(jsonContent).contains("workspaceId");
    }

    @Test
    @DisplayName("팀 수정 요청 JSON 검증")
    void validateTeamUpdateRequestJson() throws Exception {
        // Given
        TeamUpdateRequest request = new TeamUpdateRequest();
        request.setName("수정된 팀명");

        // When & Then
        String jsonContent = objectMapper.writeValueAsString(request);
        assertThat(jsonContent).contains("수정된 팀명");
    }

    /**
     * 테스트용 팀 응답 객체 생성
     */
    private TeamResponse createTeamResponse() {
        UserSummaryResponse leader = UserSummaryResponse.builder()
                .id(1L)
                .name("테스트 사용자")
                .email("test@example.com")
                .build();

        TeamResponse response = new TeamResponse();
        response.setId(1L);
        response.setName("테스트 팀");
        response.setWorkspaceId(1L);
        response.setWorkspaceName("테스트 워크스페이스");
        response.setLeader(leader);
        response.setMemberCount(1);
        response.setMembers(List.of());
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        return response;
    }

    /**
     * 테스트용 팀 멤버 응답 객체 생성
     */
    private TeamMemberResponse createTeamMemberResponse() {
        TeamMemberResponse response = new TeamMemberResponse();
        response.setId(1L);
        response.setAccountId(1L);
        response.setName("테스트 사용자");
        response.setEmail("test@example.com");
        response.setTeamRole(TeamMember.TeamRole.LEADER);
        response.setTeamStatus(TeamMember.TeamStatus.ACTIVE);
        response.setJoinedAt(LocalDateTime.now());
        response.setAge(25);
        response.setMbti("ENFP");
        response.setDisposition("적극적");
        response.setIntroduction("안녕하세요");
        return response;
    }
}
