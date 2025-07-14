package com.pickteam.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pickteam.dto.ApiResponse;
import com.pickteam.dto.security.JwtAuthenticationResponse;
import com.pickteam.dto.user.*;
import com.pickteam.service.user.AuthService;
import com.pickteam.service.user.UserService;
import com.pickteam.service.board.PostAttachService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
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
 * 사용자 컨트롤러 단위 테스트
 * @WebMvcTest를 사용하여 Presentation Layer만 테스트
 * Security 설정은 제외하고 Controller 로직만 검증
 */
@WebMvcTest(
        value = UserController.class,
        useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = UserController.class
        ),
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
        }
)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private PostAttachService postAttachService;

    @Test
    @DisplayName("사용자 회원가입을 할 수 있다")
    void registerUser_ValidRequest_ReturnsSuccess() throws Exception {
        // Given
        SignupRequest request = createSignupRequest();
        doNothing().when(userService).registerUser(any(SignupRequest.class));

        // When & Then
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists());

        verify(userService).registerUser(any(SignupRequest.class));
    }

    @Test
    @DisplayName("이메일 중복 검사를 할 수 있다")
    void checkDuplicateId_ValidEmail_ReturnsAvailability() throws Exception {
        // Given
        CheckDuplicateIdRequest request = new CheckDuplicateIdRequest();
        request.setEmail("test@example.com");

        given(userService.checkDuplicateId("test@example.com")).willReturn(false); // 중복 없음

        // When & Then
        mockMvc.perform(post("/api/users/check-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true)); // 사용 가능 (중복 아님)

        verify(userService).checkDuplicateId("test@example.com");
    }

    @Test
    @DisplayName("비밀번호 유효성 검사를 할 수 있다")
    void validatePassword_ValidPassword_ReturnsValid() throws Exception {
        // Given
        ValidatePasswordRequest request = new ValidatePasswordRequest();
        request.setPassword("ValidPass123!");

        given(userService.validatePassword("ValidPass123!")).willReturn(true);

        // When & Then
        mockMvc.perform(post("/api/users/validate-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));

        verify(userService).validatePassword("ValidPass123!");
    }

    @Test
    @DisplayName("이메일 인증을 요청할 수 있다")
    void requestEmailVerification_ValidEmail_ReturnsSuccess() throws Exception {
        // Given
        EmailVerificationRequest request = new EmailVerificationRequest();
        request.setEmail("test@gmail.com");

        doNothing().when(userService).requestEmailVerification("test@gmail.com");

        // When & Then
        mockMvc.perform(post("/api/users/email/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(userService).requestEmailVerification("test@gmail.com");
    }

    @Test
    @DisplayName("이메일 인증을 확인할 수 있다")
    void verifyEmail_ValidCode_ReturnsVerificationResult() throws Exception {
        // Given
        EmailVerificationConfirmRequest request = new EmailVerificationConfirmRequest();
        request.setEmail("test@example.com");
        request.setVerificationCode("123456");

        given(userService.verifyEmail("test@example.com", "123456")).willReturn(true);

        // When & Then
        mockMvc.perform(post("/api/users/email/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));

        verify(userService).verifyEmail("test@example.com", "123456");
    }

    @Test
    @DisplayName("로그인을 할 수 있다")
    void login_ValidCredentials_ReturnsJwtResponse() throws Exception {
        // Given
        UserLoginRequest request = createLoginRequest();
        JwtAuthenticationResponse response = createJwtResponse();

        given(authService.authenticateWithClientInfo(
                any(UserLoginRequest.class), 
                any(), 
                any()))
                .willReturn(response);

        // When & Then
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("mock-refresh-token"));

        verify(authService).authenticateWithClientInfo(any(), any(), any());
    }

    @Test
    @DisplayName("전체 사용자 프로필을 조회할 수 있다")
    void getAllUserProfile_ValidRequest_ReturnsUserList() throws Exception {
        // Given
        UserProfileResponse profile = createUserProfileResponse();
        given(userService.getAllUserProfile()).willReturn(List.of(profile));

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("테스트 사용자"))
                .andExpect(jsonPath("$.data[0].email").value("test@example.com"));

        verify(userService).getAllUserProfile();
    }

    @Test
    @DisplayName("사용자 프로필을 조회할 수 있다")
    void getUserProfile_ValidUserId_ReturnsUserProfile() throws Exception {
        // Given
        Long userId = 1L;
        UserProfileResponse profile = createUserProfileResponse();
        given(userService.getUserProfile(userId)).willReturn(profile);

        // When & Then
        mockMvc.perform(get("/api/users/{userId}", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("테스트 사용자"));

        verify(userService).getUserProfile(userId);
    }

    @Test
    @DisplayName("해시태그를 검색할 수 있다")
    void searchHashtags_ValidKeyword_ReturnsHashtagList() throws Exception {
        // Given
        String keyword = "자바";
        HashtagResponse hashtag = createHashtagResponse();
        given(userService.searchHashtags(keyword)).willReturn(List.of(hashtag));

        // When & Then
        mockMvc.perform(get("/api/users/hashtags/search")
                        .param("keyword", keyword))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Java"));

        verify(userService).searchHashtags(keyword);
    }

    @Test
    @DisplayName("JSON 요청 형식을 검증할 수 있다")
    void validateRequestJsonFormat() throws Exception {
        // Given
        SignupRequest request = createSignupRequest();

        // When & Then
        String jsonContent = objectMapper.writeValueAsString(request);
        assertThat(jsonContent).contains("test@gmail.com");
        assertThat(jsonContent).contains("Password123!");
    }

    private SignupRequest createSignupRequest() {
        SignupRequest request = new SignupRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("Password123!");
        request.setConfirmPassword("Password123!");
        return request;
    }


    private UserLoginRequest createLoginRequest() {
        UserLoginRequest request = new UserLoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        return request;
    }

    private JwtAuthenticationResponse createJwtResponse() {
        JwtAuthenticationResponse response = new JwtAuthenticationResponse();
        response.setAccessToken("mock-access-token");
        response.setRefreshToken("mock-refresh-token");
        response.setTokenType("Bearer");
        response.setExpiresIn(3600L);
        return response;
    }

    private UserProfileResponse createUserProfileResponse() {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(1L);
        response.setName("테스트 사용자");
        response.setEmail("test@example.com");
        response.setAge(25);
        response.setMbti("ENFP");
        response.setDisposition("적극적");
        response.setIntroduction("안녕하세요");
        return response;
    }

    private HashtagResponse createHashtagResponse() {
        HashtagResponse response = new HashtagResponse();
        response.setId(1L);
        response.setName("Java");
        return response;
    }

    // AssertJ import를 위한 정적 메서드
    private static org.assertj.core.api.AbstractStringAssert<?> assertThat(String actual) {
        return org.assertj.core.api.Assertions.assertThat(actual);
    }
}
