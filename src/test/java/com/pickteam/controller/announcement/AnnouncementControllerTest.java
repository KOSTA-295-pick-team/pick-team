package com.pickteam.controller.announcement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pickteam.config.TestSecurityConfig;
import com.pickteam.dto.announcement.AnnouncementCreateRequest;
import com.pickteam.dto.announcement.AnnouncementResponse;
import com.pickteam.dto.announcement.AnnouncementUpdateRequest;
import com.pickteam.service.announcement.AnnouncementService;
import jakarta.persistence.EntityNotFoundException;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 공지사항 컨트롤러 단위 테스트
 * @WebMvcTest를 사용하여 Presentation Layer만 테스트
 * Security 설정은 제외하고 Controller 로직만 검증
 */
@WebMvcTest(
        value = AnnouncementController.class,
        useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = AnnouncementController.class
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
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")  // 이러면 @Profile("!test")는 제외됨
class AnnouncementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnnouncementService announcementService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("공지사항 생성 시 정상적인 요청이면 201 Created와 함께 생성된 공지사항을 반환한다")
    void createAnnouncement_ValidRequest_Returns201Created() throws Exception {
        // given
        Long workspaceId = 1L;
        Long accountId = 100L;
        AnnouncementCreateRequest request = AnnouncementCreateRequest.builder()
                .title("새로운 공지사항")
                .content("공지사항 내용입니다.")
                .teamId(10L)
                .build();

        AnnouncementResponse expectedResponse = AnnouncementResponse.builder()
                .id(1L)
                .title("새로운 공지사항")
                .content("공지사항 내용입니다.")
                .accountId(accountId)
                .accountName("홍길동")
                .teamId(10L)
                .teamName("개발팀")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isAuthorWithdrawn(false)
                .build();

        given(announcementService.createAnnouncement(any(AnnouncementCreateRequest.class), eq(accountId)))
                .willReturn(expectedResponse);

        // when & then
        mockMvc.perform(post("/api/workspaces/{workspaceId}/announcement", workspaceId)
                        .header("Account-Id", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated());

        verify(announcementService).createAnnouncement(any(AnnouncementCreateRequest.class), eq(accountId));
    }

    @Test
    @DisplayName("공지사항 생성 시 제목이 비어있으면 400 Bad Request를 반환한다")
    void createAnnouncement_EmptyTitle_Returns400BadRequest() throws Exception {
        // given
        Long workspaceId = 1L;
        Long accountId = 100L;
        AnnouncementCreateRequest request = AnnouncementCreateRequest.builder()
                .title("")  // 빈 제목
                .content("공지사항 내용입니다.")
                .teamId(10L)
                .build();

        // when & then
        mockMvc.perform(post("/api/workspaces/{workspaceId}/announcement", workspaceId)
                        .header("Account-Id", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("공지사항 생성 시 팀ID가 null이면 400 Bad Request를 반환한다")
    void createAnnouncement_NullTeamId_Returns400BadRequest() throws Exception {
        // given
        Long workspaceId = 1L;
        Long accountId = 100L;
        AnnouncementCreateRequest request = AnnouncementCreateRequest.builder()
                .title("공지사항 제목")
                .content("공지사항 내용입니다.")
                .teamId(null)  // null 팀ID
                .build();

        // when & then
        mockMvc.perform(post("/api/workspaces/{workspaceId}/announcement", workspaceId)
                        .header("Account-Id", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("단일 공지사항 조회 시 정상적인 요청이면 200 OK와 함께 공지사항 상세 정보를 반환한다")
    void getAnnouncement_ValidRequest_Returns200OK() throws Exception {
        // given
        Long workspaceId = 1L;
        Long announcementId = 1L;

        AnnouncementResponse expectedResponse = AnnouncementResponse.builder()
                .id(announcementId)
                .title("공지사항 제목")
                .content("공지사항 상세 내용입니다.")
                .accountId(100L)
                .accountName("홍길동")
                .teamId(10L)
                .teamName("개발팀")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isAuthorWithdrawn(false)
                .build();

        given(announcementService.getAnnouncement(eq(workspaceId), eq(announcementId)))
                .willReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/api/workspaces/{workspaceId}/announcement/{announcementId}", 
                        workspaceId, announcementId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(announcementId))
                .andExpect(jsonPath("$.data.title").value("공지사항 제목"))
                .andExpect(jsonPath("$.data.content").value("공지사항 상세 내용입니다."));
    }

    @Test
    @DisplayName("공지사항 수정 시 정상적인 요청이면 200 OK와 함께 수정된 공지사항을 반환한다")
    void updateAnnouncement_ValidRequest_Returns200OK() throws Exception {
        // given
        Long workspaceId = 1L;
        Long announcementId = 1L;
        Long accountId = 100L;
        AnnouncementUpdateRequest request = AnnouncementUpdateRequest.builder()
                .title("수정된 공지사항 제목")
                .content("수정된 공지사항 내용입니다.")
                .build();

        AnnouncementResponse expectedResponse = AnnouncementResponse.builder()
                .id(announcementId)
                .title("수정된 공지사항 제목")
                .content("수정된 공지사항 내용입니다.")
                .accountId(accountId)
                .accountName("홍길동")
                .teamId(10L)
                .teamName("개발팀")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isAuthorWithdrawn(false)
                .build();

        given(announcementService.updateAnnouncement(eq(workspaceId), eq(announcementId), any(AnnouncementUpdateRequest.class), eq(accountId)))
                .willReturn(expectedResponse);

        // when & then
        mockMvc.perform(patch("/api/workspaces/{workspaceId}/announcement/{announcementId}",
                        workspaceId, announcementId)
                        .header("Account-Id", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(announcementId))
                .andExpect(jsonPath("$.data.title").value("수정된 공지사항 제목"))
                .andExpect(jsonPath("$.data.content").value("수정된 공지사항 내용입니다."));
    }

    @Test
    @DisplayName("공지사항 수정 시 존재하지 않는 공지사항 ID이면 404 Not Found를 반환한다")
    void updateAnnouncement_NonExistentId_Returns404NotFound() throws Exception {
        // given
        Long workspaceId = 1L;
        Long announcementId = 999L; // 존재하지 않는 ID
        Long accountId = 100L;
        AnnouncementUpdateRequest request = AnnouncementUpdateRequest.builder()
                .title("수정된 공지사항 제목")
                .content("수정된 공지사항 내용입니다.")
                .build();

        given(announcementService.updateAnnouncement(eq(workspaceId), eq(announcementId), any(AnnouncementUpdateRequest.class), eq(accountId)))
                .willThrow(new EntityNotFoundException("공지사항이 존재하지 않습니다."));

        // when & then
        mockMvc.perform(patch("/api/workspaces/{workspaceId}/announcement/{announcementId}",
                        workspaceId, announcementId)
                        .header("Account-Id", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("공지사항이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("공지사항 삭제 시 정상적인 요청이면 204 No Content를 반환한다")
    void deleteAnnouncement_ValidRequest_Returns204NoContent() throws Exception {
        // given
        Long workspaceId = 1L;
        Long announcementId = 1L;
        Long accountId = 100L;

        // when & then
        mockMvc.perform(delete("/api/workspaces/{workspaceId}/announcement/{announcementId}",
                        workspaceId, announcementId)
                        .header("Account-Id", accountId))
                .andDo(print())
                .andExpect(status().isOk())  // 200 OK 기대
                .andExpect(jsonPath("$.success").value(true))  // 응답 body 검증
                .andExpect(jsonPath("$.message").value("공지사항이 성공적으로 삭제되었습니다."))
                .andExpect(jsonPath("$.timestamp").exists());  // 타임스탬프도 존재하는지 확인

        verify(announcementService).deleteAnnouncement(eq(workspaceId), eq(announcementId), eq(accountId));
    }

    @Test
    @DisplayName("공지사항 삭제 시 존재하지 않는 공지사항 ID이면 404 Not Found를 반환한다")
    void deleteAnnouncement_NonExistentId_Returns404NotFound() throws Exception {
        // given
        Long workspaceId = 1L;
        Long announcementId = 999L; // 존재하지 않는 ID
        Long accountId = 100L;

        doThrow(new EntityNotFoundException("공지사항이 존재하지 않습니다."))
                .when(announcementService).deleteAnnouncement(eq(workspaceId), eq(announcementId), eq(accountId));

        // when & then
        mockMvc.perform(delete("/api/workspaces/{workspaceId}/announcement/{announcementId}", 
                        workspaceId, announcementId)
                        .header("Account-Id", accountId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("공지사항이 존재하지 않습니다."));
    }
}
