package com.pickteam.controller.video;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pickteam.config.TestSecurityConfig;
import com.pickteam.controller.VideoConferenceController;
import com.pickteam.dto.VideoChannelDTO;
import com.pickteam.dto.VideoMemberDTO;
import com.pickteam.exception.VideoConferenceErrorCode;
import com.pickteam.exception.VideoConferenceException;
import com.pickteam.service.VideoConferenceService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 화상회의 컨트롤러 단위 테스트
 * @WebMvcTest를 사용하여 Presentation Layer만 테스트
 * Security 설정은 제외하고 Controller 로직만 검증
 */
/**
@WebMvcTest(
        value = VideoConferenceController.class,
        useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = VideoConferenceController.class
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
@ActiveProfiles("test")
class VideoConferenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VideoConferenceService videoConferenceService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("비디오 채널 목록 조회 시 정상적인 요청이면 200 OK와 함께 채널 목록을 반환한다")
    void getChannels_ValidRequest_Returns200OK() throws Exception {
        // given
        Long workspaceId = 1L;
        Long accountId = 100L;
        
        VideoChannelDTO channel1 = new VideoChannelDTO();
        channel1.setId(1L);
        channel1.setName("일반 회의실");
        channel1.setCreatedAt(LocalDateTime.now());
        
        VideoChannelDTO channel2 = new VideoChannelDTO();
        channel2.setId(2L);
        channel2.setName("개발팀 회의실");
        channel2.setCreatedAt(LocalDateTime.now());
        
        List<VideoChannelDTO> expectedChannels = List.of(channel1, channel2);

        given(videoConferenceService.selectVideoChannels(workspaceId, accountId))
                .willReturn(expectedChannels);

        // when & then
        mockMvc.perform(get("/api/workspaces/{workspaceId}/video-channels", workspaceId)
                        .param("accountId", String.valueOf(accountId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("일반 회의실"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("개발팀 회의실"));

        verify(videoConferenceService).selectVideoChannels(workspaceId, accountId);
    }

    @Test
    @DisplayName("비디오 채널 목록 조회 시 accountId 없이도 정상적으로 동작한다")
    void getChannels_WithoutAccountId_Returns200OK() throws Exception {
        // given
        Long workspaceId = 1L;
        List<VideoChannelDTO> expectedChannels = List.of();

        given(videoConferenceService.selectVideoChannels(eq(workspaceId), isNull()))
                .willReturn(expectedChannels);

        // when & then
        mockMvc.perform(get("/api/workspaces/{workspaceId}/video-channels", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(videoConferenceService).selectVideoChannels(eq(workspaceId), isNull());
    }

    @Test
    @DisplayName("비디오 채널 생성 시 정상적인 요청이면 201 Created를 반환한다")
    void createChannel_ValidRequest_Returns201Created() throws Exception {
        // given
        Long workspaceId = 1L;
        VideoChannelDTO request = new VideoChannelDTO();
        request.setName("새로운 회의실");

        // when & then
        mockMvc.perform(post("/api/workspaces/{workspaceId}/video-channels", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated());

        verify(videoConferenceService).insertVideoChannel(workspaceId, "새로운 회의실");
    }

    @Test
    @DisplayName("비디오 채널 생성 시 채널 이름이 비어있으면 400 Bad Request를 반환한다")
    void createChannel_EmptyName_Returns400BadRequest() throws Exception {
        // given
        Long workspaceId = 1L;
        VideoChannelDTO request = new VideoChannelDTO();
        request.setName(""); // 빈 이름

        // when & then
        mockMvc.perform(post("/api/workspaces/{workspaceId}/video-channels", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("비디오 채널 생성 시 채널 이름이 null이면 400 Bad Request를 반환한다")
    void createChannel_NullName_Returns400BadRequest() throws Exception {
        // given
        Long workspaceId = 1L;
        VideoChannelDTO request = new VideoChannelDTO();
        request.setName(null); // null 이름

        // when & then
        mockMvc.perform(post("/api/workspaces/{workspaceId}/video-channels", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("비디오 채널 삭제 시 정상적인 요청이면 204 No Content를 반환한다")
    void deleteChannel_ValidRequest_Returns204NoContent() throws Exception {
        // given
        Long channelId = 1L;

        // when & then
        mockMvc.perform(delete("/api/workspaces/1/video-channels/{channelId}", channelId))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(videoConferenceService).deleteVideoChannel(channelId);
    }

    @Test
    @DisplayName("비디오 채널 삭제 시 존재하지 않는 채널이면 404 Not Found를 반환한다")
    void deleteChannel_ChannelNotFound_Returns404NotFound() throws Exception {
        // given
        Long channelId = 999L;
        doThrow(new VideoConferenceException(VideoConferenceErrorCode.CHANNEL_NOT_FOUND))
                .when(videoConferenceService).deleteVideoChannel(channelId);

        // when & then
        mockMvc.perform(delete("/api/workspaces/1/video-channels/{channelId}", channelId))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(videoConferenceService).deleteVideoChannel(channelId);
    }

    @Test
    @DisplayName("비디오 채널 참가자 목록 조회 시 정상적인 요청이면 200 OK와 함께 참가자 목록을 반환한다")
    void getParticipants_ValidRequest_Returns200OK() throws Exception {
        // given
        Long channelId = 1L;
        
        VideoMemberDTO member1 = new VideoMemberDTO();
        member1.setId(1L);
        member1.setUserId(100L);
        member1.setEmail("user1@example.com");
        member1.setName("사용자1");
        member1.setJoinDate(LocalDateTime.now());
        
        VideoMemberDTO member2 = new VideoMemberDTO();
        member2.setId(2L);
        member2.setUserId(200L);
        member2.setEmail("user2@example.com");
        member2.setName("사용자2");
        member2.setJoinDate(LocalDateTime.now());
        
        List<VideoMemberDTO> expectedMembers = List.of(member1, member2);

        given(videoConferenceService.selectVideoChannelParticipants(channelId))
                .willReturn(expectedMembers);

        // when & then
        mockMvc.perform(get("/api/workspaces/1/video-channels/{channelId}/video-members", channelId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].email").value("user1@example.com"))
                .andExpect(jsonPath("$[0].name").value("사용자1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].email").value("user2@example.com"))
                .andExpect(jsonPath("$[1].name").value("사용자2"));

        verify(videoConferenceService).selectVideoChannelParticipants(channelId);
    }

    @Test
    @DisplayName("비디오 채널 참가자 목록 조회 시 존재하지 않는 채널이면 404 Not Found를 반환한다")
    void getParticipants_ChannelNotFound_Returns404NotFound() throws Exception {
        // given
        Long channelId = 999L;
        doThrow(new VideoConferenceException(VideoConferenceErrorCode.CHANNEL_NOT_FOUND))
                .when(videoConferenceService).selectVideoChannelParticipants(channelId);

        // when & then
        mockMvc.perform(get("/api/workspaces/1/video-channels/{channelId}/video-members", channelId))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(videoConferenceService).selectVideoChannelParticipants(channelId);
    }

    @Test
    @DisplayName("비디오 채널 나가기 시 정상적인 요청이면 204 No Content를 반환한다")
    void leaveChannel_ValidRequest_Returns204NoContent() throws Exception {
        // given
        Long channelId = 1L;
        Long memberId = 100L;

        // when & then
        mockMvc.perform(delete("/api/workspaces/1/video-channels/{channelId}/video-members/{memberId}", channelId, memberId))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(videoConferenceService).deleteVideoChannelParticipant(memberId);
    }

    @Test
    @DisplayName("비디오 채널 나가기 시 존재하지 않는 멤버면 404 Not Found를 반환한다")
    void leaveChannel_MemberNotFound_Returns404NotFound() throws Exception {
        // given
        Long channelId = 1L;
        Long memberId = 999L;
        doThrow(new VideoConferenceException(VideoConferenceErrorCode.MEMBER_NOT_FOUND))
                .when(videoConferenceService).deleteVideoChannelParticipant(memberId);

        // when & then
        mockMvc.perform(delete("/api/workspaces/1/video-channels/{channelId}/video-members/{memberId}", channelId, memberId))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(videoConferenceService).deleteVideoChannelParticipant(memberId);
    }

    @Test
    @DisplayName("VideoConferenceException 발생 시 적절한 HTTP 상태코드와 에러 메시지를 반환한다")
    void handleVideoConferenceException_ReturnsProperErrorResponse() throws Exception {
        // given
        Long workspaceId = 1L;
        doThrow(new VideoConferenceException(VideoConferenceErrorCode.CHANNEL_NOT_FOUND))
                .when(videoConferenceService).selectVideoChannels(eq(workspaceId), isNull());

        // when & then
        mockMvc.perform(get("/api/workspaces/{workspaceId}/video-channels", workspaceId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value(VideoConferenceErrorCode.CHANNEL_NOT_FOUND.getTitle()))
                .andExpect(jsonPath("$.detail").value(VideoConferenceErrorCode.CHANNEL_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("일반 Exception 발생 시 500 Internal Server Error를 반환한다")
    void handleGenericException_Returns500InternalServerError() throws Exception {
        // given
        Long workspaceId = 1L;
        doThrow(new RuntimeException("예기치 않은 오류"))
                .when(videoConferenceService).selectVideoChannels(eq(workspaceId), isNull());

        // when & then
        mockMvc.perform(get("/api/workspaces/{workspaceId}/video-channels", workspaceId))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.detail").value("알 수 없는 이유로 오류가 발생하였습니다"));
    }
}
 **/