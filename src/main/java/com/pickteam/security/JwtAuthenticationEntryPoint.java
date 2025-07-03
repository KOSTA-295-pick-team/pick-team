package com.pickteam.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pickteam.exception.common.ProblemDetail;
import com.pickteam.exception.common.ProblemType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 인증 실패 핸들러
 * - 인증되지 않은 요청에 대한 401 Unauthorized 응답 처리
 * - RFC 9457 표준 ProblemDetail 형태의 에러 응답 제공
 * - 구조화된 JSON 형태의 에러 응답 처리 (ObjectMapper 사용)
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

        // RFC 9457 표준 ProblemDetail 에러 응답 생성
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("timestamp", LocalDateTime.now());
        extensions.put("path", request.getRequestURI());

        ProblemDetail errorResponse = ProblemDetail.builder()
                .type(ProblemType.UNAUTHORIZED_ACCESS.getType())
                .title(ProblemType.UNAUTHORIZED_ACCESS.getTitle())
                .status(HttpStatus.UNAUTHORIZED.value())
                .detail("인증이 필요합니다. 로그인 후 다시 시도해주세요.")
                .instance("/authentication-required")
                .extensions(extensions)
                .build();

        // ObjectMapper를 사용한 안전한 JSON 직렬화
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
