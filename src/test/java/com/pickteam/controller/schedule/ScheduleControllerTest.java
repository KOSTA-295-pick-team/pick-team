package com.pickteam.controller.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pickteam.domain.schedule.ScheduleType;
import com.pickteam.dto.schedule.ScheduleCreateDto;
import com.pickteam.dto.schedule.ScheduleResponseDto;
import com.pickteam.dto.schedule.ScheduleUpdateDto;
import com.pickteam.service.schedule.ScheduleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 일정 컨트롤러 단위 테스트
 * @WebMvcTest를 사용하여 Presentation Layer만 테스트
 * Security 설정은 제외하고 Controller 로직만 검증
 */
@WebMvcTest(
        value = ScheduleController.class,
        useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = ScheduleController.class
        ),
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
        }
)
@ActiveProfiles("test")
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ScheduleService scheduleService;

    @Test
    @DisplayName("일정을 생성할 수 있다")
    void createSchedule_ValidRequest_ReturnsScheduleResponseDto() throws Exception {
        // Given
        Long teamId = 1L;
        Long accountId = 1L;
        ScheduleCreateDto request = createScheduleCreateDto();
        ScheduleResponseDto response = createScheduleResponseDto();

        given(scheduleService.createSchedule(any(ScheduleCreateDto.class), eq(teamId), eq(accountId)))
                .willReturn(response);

        // When & Then
        mockMvc.perform(post("/api/teams/{teamId}/schedules", teamId)
                        .param("accountId", accountId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("테스트 일정"))
                .andExpect(jsonPath("$.type").value("MEETING"))
                .andExpect(jsonPath("$.scheduleDesc").value("테스트 일정 설명"));

        verify(scheduleService).createSchedule(any(ScheduleCreateDto.class), eq(teamId), eq(accountId));
    }

    @Test
    @DisplayName("팀별 일정 목록을 조회할 수 있다")
    void getSchedules_ValidTeamId_ReturnsSchedulesList() throws Exception {
        // Given
        Long teamId = 1L;
        ScheduleResponseDto schedule = createScheduleResponseDto();
        Page<ScheduleResponseDto> schedulePage = new PageImpl<>(List.of(schedule));

        given(scheduleService.getSchedules(eq(teamId), any(Pageable.class)))
                .willReturn(schedulePage);

        // When & Then
        mockMvc.perform(get("/api/teams/{teamId}/schedules", teamId)
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("테스트 일정"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(scheduleService).getSchedules(eq(teamId), any(Pageable.class));
    }

    @Test
    @DisplayName("일정 타입별로 일정을 조회할 수 있다")
    void getSchedules_WithType_ReturnsFilteredSchedules() throws Exception {
        // Given
        Long teamId = 1L;
        ScheduleResponseDto schedule = createScheduleResponseDto();
        Page<ScheduleResponseDto> schedulePage = new PageImpl<>(List.of(schedule));

        given(scheduleService.getSchedulesByType(eq(teamId), eq(ScheduleType.MEETING), any(Pageable.class)))
                .willReturn(schedulePage);

        // When & Then
        mockMvc.perform(get("/api/teams/{teamId}/schedules", teamId)
                        .param("page", "0")
                        .param("size", "20")
                        .param("type", "MEETING"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value("MEETING"));

        verify(scheduleService).getSchedulesByType(eq(teamId), eq(ScheduleType.MEETING), any(Pageable.class));
    }

    @Test
    @DisplayName("기간별 일정을 조회할 수 있다")
    void getSchedulesByDateRange_ValidDateRange_ReturnsSchedules() throws Exception {
        // Given
        Long teamId = 1L;
        ScheduleResponseDto schedule = createScheduleResponseDto();
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        given(scheduleService.getSchedulesByDateRange(eq(teamId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(List.of(schedule));

        // When & Then
        mockMvc.perform(get("/api/teams/{teamId}/schedules/range", teamId)
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("테스트 일정"));

        verify(scheduleService).getSchedulesByDateRange(eq(teamId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("내 일정을 조회할 수 있다")
    void getMySchedules_ValidAccountId_ReturnsMySchedules() throws Exception {
        // Given
        Long teamId = 1L;
        Long accountId = 1L;
        ScheduleResponseDto schedule = createScheduleResponseDto();
        Page<ScheduleResponseDto> schedulePage = new PageImpl<>(List.of(schedule));

        given(scheduleService.getMySchedules(eq(accountId), any(Pageable.class)))
                .willReturn(schedulePage);

        // When & Then
        mockMvc.perform(get("/api/teams/{teamId}/schedules/my", teamId)
                        .param("accountId", accountId.toString())
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));

        verify(scheduleService).getMySchedules(eq(accountId), any(Pageable.class));
    }

    @Test
    @DisplayName("일정을 수정할 수 있다")
    void updateSchedule_ValidRequest_ReturnsUpdatedSchedule() throws Exception {
        // Given
        Long teamId = 1L;
        Long scheduleId = 1L;
        Long accountId = 1L;
        ScheduleUpdateDto request = createScheduleUpdateDto();
        ScheduleResponseDto response = createUpdatedScheduleResponseDto();

        given(scheduleService.updateSchedule(eq(scheduleId), any(ScheduleUpdateDto.class), eq(accountId)))
                .willReturn(response);

        // When & Then
        mockMvc.perform(put("/api/teams/{teamId}/schedules/{scheduleId}", teamId, scheduleId)
                        .param("accountId", accountId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("수정된 일정"))
                .andExpect(jsonPath("$.type").value("DEADLINE"));

        verify(scheduleService).updateSchedule(eq(scheduleId), any(ScheduleUpdateDto.class), eq(accountId));
    }

    @Test
    @DisplayName("일정을 삭제할 수 있다")
    void deleteSchedule_ValidRequest_ReturnsNoContent() throws Exception {
        // Given
        Long teamId = 1L;
        Long scheduleId = 1L;
        Long accountId = 1L;

        doNothing().when(scheduleService).deleteSchedule(scheduleId, accountId);

        // When & Then
        mockMvc.perform(delete("/api/teams/{teamId}/schedules/{scheduleId}", teamId, scheduleId)
                        .param("accountId", accountId.toString()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(scheduleService).deleteSchedule(scheduleId, accountId);
    }

    private ScheduleCreateDto createScheduleCreateDto() {
        ScheduleCreateDto dto = new ScheduleCreateDto();
        dto.setTitle("테스트 일정");
        dto.setStartDate(LocalDateTime.now());
        dto.setEndDate(LocalDateTime.now().plusHours(2));
        dto.setScheduleDesc("테스트 일정 설명");
        dto.setType(ScheduleType.MEETING);
        return dto;
    }

    private ScheduleUpdateDto createScheduleUpdateDto() {
        ScheduleUpdateDto dto = new ScheduleUpdateDto();
        dto.setTitle("수정된 일정");
        dto.setStartDate(LocalDateTime.now().plusDays(1));
        dto.setEndDate(LocalDateTime.now().plusDays(1).plusHours(2));
        dto.setScheduleDesc("수정된 일정 설명");
        dto.setType(ScheduleType.DEADLINE);
        return dto;
    }

    private ScheduleResponseDto createScheduleResponseDto() {
        ScheduleResponseDto dto = new ScheduleResponseDto();
        dto.setId(1L);
        dto.setTitle("테스트 일정");
        dto.setStartDate(LocalDateTime.now());
        dto.setEndDate(LocalDateTime.now().plusHours(2));
        dto.setScheduleDesc("테스트 일정 설명");
        dto.setType(ScheduleType.MEETING);
        dto.setCreatorName("테스트 사용자");
        dto.setTeamName("테스트 팀");
        return dto;
    }

    private ScheduleResponseDto createUpdatedScheduleResponseDto() {
        ScheduleResponseDto dto = new ScheduleResponseDto();
        dto.setId(1L);
        dto.setTitle("수정된 일정");
        dto.setStartDate(LocalDateTime.now().plusDays(1));
        dto.setEndDate(LocalDateTime.now().plusDays(1).plusHours(2));
        dto.setScheduleDesc("수정된 일정 설명");
        dto.setType(ScheduleType.DEADLINE);
        dto.setCreatorName("테스트 사용자");
        dto.setTeamName("테스트 팀");
        return dto;
    }
}
