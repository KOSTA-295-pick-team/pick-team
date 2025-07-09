package com.pickteam.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pickteam.exception.common.ProblemDetail;
import com.pickteam.exception.common.ProblemType;
import com.pickteam.constants.SessionErrorCode;
import com.pickteam.repository.user.RefreshTokenRepository;
import com.pickteam.repository.user.AccountRepository;
import com.pickteam.domain.user.RefreshToken;
import com.pickteam.service.security.SecurityAuditLogger;
import com.pickteam.util.ClientInfoExtractor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT 인증 필터
 * - 모든 HTTP 요청에서 JWT 토큰을 검사하고 인증 처리
 * - Authorization 헤더에서 Bearer 토큰 추출
 * - 토큰 유효성 검증 후 Spring Security Context에 인증 정보 설정
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccountRepository accountRepository;
    private final ObjectMapper objectMapper;
    private final SecurityAuditLogger securityAuditLogger;

    /**
     * 요청별 JWT 토큰 검증 및 인증 처리
     * - 토큰 추출 → 유효성 검증 → 세션 유효성 검증 → 사용자 정보 로드 → 인증 객체 생성
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                Long userId = tokenProvider.getUserIdFromToken(jwt);
                String email = tokenProvider.getEmailFromToken(jwt);

                // 세션 유효성 추가 검증
                if (!isSessionValid(userId, email, request)) {
                    handleSessionExpired(response, email, request);
                    return;
                }

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.warn("JWT 인증 처리 중 오류가 발생했습니다. URI: {}", request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }

    /** Authorization 헤더에서 Bearer 토큰 추출 */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            log.debug("JWT 토큰 추출 완료 - 길이: {}, 시작: {}",
                    token.length(),
                    token.length() > 20 ? token.substring(0, 20) + "..." : token);
            return token;
        }
        log.debug("Authorization 헤더에서 유효한 Bearer 토큰을 찾을 수 없음. 헤더 값: {}", bearerToken);
        return null;
    }

    /**
     * 세션 유효성 검증
     * - RefreshToken 존재 여부와 유효성으로 세션 유효성 확인
     * - 의심스러운 활동 로깅
     * 
     * @param userId  사용자 ID
     * @param email   사용자 이메일
     * @param request HTTP 요청
     * @return 세션이 유효하면 true, 그렇지 않으면 false
     */
    private boolean isSessionValid(Long userId, String email, HttpServletRequest request) {
        try {
            return accountRepository.findByIdAndDeletedAtIsNull(userId)
                    .map(account -> {
                        List<RefreshToken> tokens = refreshTokenRepository.findByAccount(account);
                        if (tokens.isEmpty()) {
                            // 유효한 세션이 없음을 로깅
                            ClientInfoExtractor.ClientInfo clientInfo = ClientInfoExtractor.extractClientInfo(request);
                            securityAuditLogger.logExpiredTokenAccess(email, clientInfo.getIpAddress(),
                                    clientInfo.getUserAgent());
                            return false;
                        }
                        return tokens.stream().anyMatch(RefreshToken::isValid);
                    })
                    .orElse(false);
        } catch (Exception e) {
            log.warn("세션 유효성 검증 중 오류 발생: userId={}", userId, e);
            return false;
        }
    }

    /**
     * 세션 만료 처리
     * - 401 상태코드와 함께 구체적인 세션 만료 메시지 반환
     * - SessionErrorCode를 사용한 표준화된 에러 응답
     * - 보안 로깅 추가
     * 
     * @param response HTTP 응답 객체
     * @param email    사용자 이메일
     * @param request  HTTP 요청
     * @throws IOException JSON 응답 작성 중 오류 발생 시
     */
    private void handleSessionExpired(HttpServletResponse response, String email, HttpServletRequest request)
            throws IOException {
        log.warn("세션이 만료되어 요청을 거부합니다 - 다른 기기에서 로그인됨: email={}", email);

        // 보안 이벤트 로깅
        ClientInfoExtractor.ClientInfo clientInfo = ClientInfoExtractor.extractClientInfo(request);
        securityAuditLogger.logInvalidTokenAccess(clientInfo.getIpAddress(), clientInfo.getUserAgent(), "세션 만료");

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // RFC 9457 표준 ProblemDetail 에러 응답 생성
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("timestamp", LocalDateTime.now());
        extensions.put("errorCode", SessionErrorCode.DUPLICATE_LOGIN.getCode());
        extensions.put("path", request.getRequestURI());

        ProblemDetail errorResponse = ProblemDetail.builder()
                .type(ProblemType.SESSION_EXPIRED.getType())
                .title(ProblemType.SESSION_EXPIRED.getTitle())
                .status(HttpServletResponse.SC_UNAUTHORIZED)
                .detail("다른 기기에서 로그인되어 현재 세션이 만료되었습니다. 다시 로그인해주세요.")
                .instance("/session-expired")
                .extensions(extensions)
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
