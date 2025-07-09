package com.pickteam.controller.user;

import com.pickteam.domain.enums.AuthProvider;
import com.pickteam.dto.ApiResponse;
import com.pickteam.dto.security.JwtAuthenticationResponse;
import com.pickteam.service.user.OAuthService;
import com.pickteam.service.user.AuthService;
import com.pickteam.service.user.OAuthStateService;
import com.pickteam.service.user.OAuthTempCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.net.URI;

/**
 * OAuth 소셜 로그인 컨트롤러
 * - 구글, 카카오 OAuth 로그인 처리
 * - 자동 회원가입 및 JWT 토큰 발급
 * - 계정 연동/해제 기능 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/oauth")
@RequiredArgsConstructor
public class OAuthController {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final OAuthService oauthService;
    private final AuthService authService;
    private final OAuthStateService oauthStateService;
    private final OAuthTempCodeService oauthTempCodeService;

    /**
     * OAuth 로그인 URL 생성 및 리다이렉트
     * 
     * @param provider OAuth 제공자 (google, kakao)
     * @param response HTTP 응답 (리다이렉트용)
     * @throws IOException 리다이렉트 실패 시
     */
    @GetMapping("/{provider}/login")
    public void redirectToOAuthProvider(@PathVariable String provider, HttpServletResponse response)
            throws IOException {
        log.info("OAuth 로그인 요청 - 제공자: {}", provider);

        try {
            // 제공자 검증 및 변환
            AuthProvider authProvider = AuthProvider.fromString(provider);

            if (!authProvider.isSocial()) {
                log.warn("소셜 로그인이 아닌 제공자 요청: {}", provider);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "지원하지 않는 OAuth 제공자입니다");
                return;
            }

            // OAuth URL 생성
            String oauthUrl = oauthService.generateOAuthUrl(authProvider);

            log.info("OAuth URL 생성 완료 - 제공자: {}, 리다이렉트 시작", provider);

            // OAuth 제공자로 리다이렉트
            response.sendRedirect(oauthUrl);

        } catch (IllegalArgumentException e) {
            log.error("잘못된 OAuth 제공자: {}", provider, e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "지원하지 않는 OAuth 제공자입니다: " + provider);
        } catch (Exception e) {
            log.error("OAuth URL 생성 실패 - 제공자: {}", provider, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "OAuth 로그인 처리 중 오류가 발생했습니다");
        }
    }

    /**
     * OAuth 콜백 처리 (Authorization Code → JWT 토큰)
     * 
     * @param provider OAuth 제공자
     * @param code     Authorization Code
     * @param state    CSRF 방지용 state
     * @param error    OAuth 인증 실패 시 오류 코드
     * @param request  HTTP 요청 (클라이언트 정보 추출용)
     * @return JWT 토큰 응답
     */
    @GetMapping("/{provider}/callback")
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> handleOAuthCallback(
            @PathVariable String provider,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            HttpServletRequest request) {

        log.info("OAuth 콜백 수신 - 제공자: {}, 코드 존재: {}, 에러: {}", provider, code != null, error);

        try {
            // CSRF 방지를 위한 state 검증 (더 상세한 로깅)
            if (state == null || state.trim().isEmpty()) {
                log.warn("OAuth state 검증 실패 - 제공자: {}, 이유: state 파라미터 누락", provider);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("잘못된 요청입니다. (CSRF 보호)"));
            }

            if (!oauthStateService.validateState(state)) {
                log.warn("OAuth state 검증 실패 - 제공자: {}, 이유: state 값 불일치 또는 만료", provider);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("잘못된 요청입니다. (CSRF 보호)"));
            }

            log.debug("OAuth state 검증 성공 - 제공자: {}", provider);

            // OAuth 인증 실패 확인
            if (error != null) {
                log.warn("OAuth 인증 실패 - 제공자: {}, 에러: {}", provider, error);
                oauthStateService.clearStoredState(); // 실패 시 state 정리
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("OAuth 인증이 취소되었거나 실패했습니다: " + error));
            }

            // Authorization Code 확인
            if (code == null || code.trim().isEmpty()) {
                log.warn("OAuth Authorization Code 누락 - 제공자: {}", provider);
                oauthStateService.clearStoredState(); // 실패 시 state 정리
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("OAuth 인증 코드가 제공되지 않았습니다"));
            }

            // 제공자 검증 및 변환
            AuthProvider authProvider = AuthProvider.fromString(provider);

            if (!authProvider.isSocial()) {
                log.warn("소셜 로그인이 아닌 제공자 콜백: {}", provider);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("지원하지 않는 OAuth 제공자입니다: " + provider));
            }

            // OAuth 로그인 처리 (자동 회원가입 포함)
            JwtAuthenticationResponse jwtResponse = oauthService.processOAuthLogin(authProvider, code);

            log.info("OAuth 로그인 성공 - 제공자: {}", provider);

            // 보안을 위해 임시 코드 생성 및 JWT 토큰 저장
            String tempCode = oauthTempCodeService.generateTempCodeAndStoreTokens(jwtResponse);

            // 프론트엔드로 임시 코드만 전달 (토큰은 별도 API로 교환)
            String redirectUrl = String.format("%s/oauth/success?code=%s", frontendUrl, tempCode);

            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(redirectUrl))
                    .build();

        } catch (IllegalArgumentException e) {
            log.error("잘못된 OAuth 제공자: {}", provider, e);
            oauthStateService.clearStoredState(); // 예외 발생 시 state 정리
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("지원하지 않는 OAuth 제공자입니다: " + provider));
        } catch (Exception e) {
            log.error("OAuth 콜백 처리 실패 - 제공자: {}, 코드: {}", provider, code != null ? "있음" : "없음", e);
            oauthStateService.clearStoredState(); // 예외 발생 시 state 정리
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("OAuth 로그인 처리 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 임시 코드를 JWT 토큰으로 교환
     * 
     * @param tempCode 임시 코드
     * @return JWT 토큰 응답
     */
    @PostMapping("/exchange-token")
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> exchangeTokenWithTempCode(
            @RequestParam String tempCode) {

        log.info("OAuth 임시 코드 토큰 교환 요청");

        try {
            // 임시 코드로 JWT 토큰 조회 및 제거
            JwtAuthenticationResponse jwtResponse = oauthTempCodeService.exchangeTokensWithTempCode(tempCode);

            if (jwtResponse == null) {
                log.warn("OAuth 임시 코드 토큰 교환 실패: 유효하지 않은 코드");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("유효하지 않거나 만료된 코드입니다"));
            }

            log.info("OAuth 임시 코드 토큰 교환 성공");
            return ResponseEntity.ok(ApiResponse.success("토큰 교환 성공", jwtResponse));

        } catch (Exception e) {
            log.error("OAuth 임시 코드 토큰 교환 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("토큰 교환 처리 중 오류가 발생했습니다"));
        }
    }

    /**
     * 기존 계정에 OAuth 계정 연동
     * 
     * @param provider OAuth 제공자
     * @param code     Authorization Code
     * @return 연동 성공 메시지
     */
    @PostMapping("/{provider}/link")
    public ResponseEntity<ApiResponse<Void>> linkOAuthAccount(
            @PathVariable String provider,
            @RequestParam String code) {

        log.info("OAuth 계정 연동 요청 - 제공자: {}", provider);

        try {
            // 현재 로그인된 사용자 ID 조회
            Long currentUserId = authService.requireAuthentication();

            // 제공자 검증 및 변환
            AuthProvider authProvider = AuthProvider.fromString(provider);

            if (!authProvider.isSocial()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("지원하지 않는 OAuth 제공자입니다: " + provider));
            }

            // OAuth 계정 연동
            oauthService.linkOAuthAccount(currentUserId, authProvider, code);

            log.info("OAuth 계정 연동 성공 - 사용자 ID: {}, 제공자: {}", currentUserId, provider);

            return ResponseEntity.ok(ApiResponse.success(
                    authProvider.getDescription() + " 계정 연동이 완료되었습니다", null));

        } catch (IllegalArgumentException e) {
            log.error("잘못된 OAuth 제공자: {}", provider, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("지원하지 않는 OAuth 제공자입니다: " + provider));
        } catch (RuntimeException e) {
            log.error("OAuth 계정 연동 실패 - 제공자: {}", provider, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("OAuth 계정 연동 처리 중 오류 - 제공자: {}", provider, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("계정 연동 처리 중 오류가 발생했습니다"));
        }
    }

    /**
     * OAuth 계정 연동 해제
     * 
     * @param provider OAuth 제공자
     * @return 연동 해제 성공 메시지
     */
    @DeleteMapping("/{provider}/unlink")
    public ResponseEntity<ApiResponse<Void>> unlinkOAuthAccount(@PathVariable String provider) {

        log.info("OAuth 계정 연동 해제 요청 - 제공자: {}", provider);

        try {
            // 현재 로그인된 사용자 ID 조회
            Long currentUserId = authService.requireAuthentication();

            // 제공자 검증 및 변환
            AuthProvider authProvider = AuthProvider.fromString(provider);

            if (!authProvider.isSocial()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("지원하지 않는 OAuth 제공자입니다: " + provider));
            }

            // OAuth 계정 연동 해제
            oauthService.unlinkOAuthAccount(currentUserId, authProvider);

            log.info("OAuth 계정 연동 해제 성공 - 사용자 ID: {}, 제공자: {}", currentUserId, provider);

            return ResponseEntity.ok(ApiResponse.success(
                    authProvider.getDescription() + " 계정 연동이 해제되었습니다", null));

        } catch (IllegalArgumentException e) {
            log.error("잘못된 OAuth 제공자: {}", provider, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("지원하지 않는 OAuth 제공자입니다: " + provider));
        } catch (RuntimeException e) {
            log.error("OAuth 계정 연동 해제 실패 - 제공자: {}", provider, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("OAuth 계정 연동 해제 처리 중 오류 - 제공자: {}", provider, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("계정 연동 해제 처리 중 오류가 발생했습니다"));
        }
    }

    /**
     * OAuth 계정 연동 상태 확인
     * 
     * @param provider OAuth 제공자
     * @return 연동 상태 (true: 연동됨, false: 연동 안됨)
     */
    @GetMapping("/{provider}/status")
    public ResponseEntity<ApiResponse<Boolean>> checkOAuthLinkStatus(@PathVariable String provider) {

        log.debug("OAuth 계정 연동 상태 확인 - 제공자: {}", provider);

        try {
            // 현재 로그인된 사용자 ID 조회
            Long currentUserId = authService.requireAuthentication();

            // 제공자 검증 및 변환
            AuthProvider authProvider = AuthProvider.fromString(provider);

            if (!authProvider.isSocial()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("지원하지 않는 OAuth 제공자입니다: " + provider));
            }

            // OAuth 연동 상태 확인
            boolean isLinked = oauthService.isOAuthLinked(currentUserId, authProvider);

            log.debug("OAuth 계정 연동 상태 - 사용자 ID: {}, 제공자: {}, 연동 상태: {}",
                    currentUserId, provider, isLinked);

            return ResponseEntity.ok(ApiResponse.success("연동 상태 조회 완료", isLinked));

        } catch (IllegalArgumentException e) {
            log.error("잘못된 OAuth 제공자: {}", provider, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("지원하지 않는 OAuth 제공자입니다: " + provider));
        } catch (Exception e) {
            log.error("OAuth 계정 연동 상태 확인 중 오류 - 제공자: {}", provider, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("연동 상태 확인 중 오류가 발생했습니다"));
        }
    }
}
