package com.pickteam.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pickteam.dto.ApiResponse;
import com.pickteam.constants.SessionErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT 인증 실패 핸들러
 * - 인증되지 않은 요청에 대한 401 Unauthorized 응답 처리
 * - 구조화된 JSON 형태의 에러 응답 제공 (ObjectMapper 사용)
 * - 국제화 준비된 영어 메시지 사용
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    /** 인증 실패 시 호출되는 메서드 */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        log.warn("인증되지 않은 요청이 감지되었습니다. URI: {}", request.getRequestURI());

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // 구조화된 에러 응답 생성 - SessionErrorCode 사용
        ApiResponse<Void> errorResponse = ApiResponse.error(SessionErrorCode.SESSION_INVALID);

        // ObjectMapper를 사용한 안전한 JSON 직렬화
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
