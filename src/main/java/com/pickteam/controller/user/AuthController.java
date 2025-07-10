package com.pickteam.controller.user;

import com.pickteam.dto.user.*;
import com.pickteam.dto.user.PasswordResetDto.*;
import com.pickteam.dto.security.JwtAuthenticationResponse;
import com.pickteam.dto.security.RefreshTokenRequest;
import com.pickteam.dto.ApiResponse;
import com.pickteam.service.user.UserService;
import com.pickteam.service.user.AuthService;
import com.pickteam.constants.UserControllerMessages;
import com.pickteam.exception.validation.ValidationException;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

/**
 * 인증 관련 API를 담당하는 컨트롤러
 * - 로그인/로그아웃
 * - 토큰 관리 (발급, 갱신)
 * - 비밀번호 찾기/재설정
 * - 이메일 인증
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    // 환경변수에서 주입받는 설정들
    @Value("${app.email.blocked-domains}")
    private String blockedDomainsConfig;

    // ==================== 기본 인증 API ====================

    /**
     * 로그인 API
     * 클라이언트 정보를 포함한 강화된 로그인 처리
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> login(
            @Valid @RequestBody UserLoginRequest request,
            @RequestBody(required = false) SessionInfoRequest sessionInfo,
            jakarta.servlet.http.HttpServletRequest httpRequest) {

        log.info("로그인 시도 - 이메일: {}", maskEmail(request.getEmail()));

        // 추가 검증: 비밀번호 최소 길이 체크 (보안 강화)
        if (request.getPassword() != null && request.getPassword().length() < 8) {
            log.warn("너무 짧은 비밀번호로 로그인 시도: 이메일={}", maskEmail(request.getEmail()));
            throw new ValidationException("비밀번호는 최소 8자 이상이어야 합니다.");
        }

        JwtAuthenticationResponse response = authService.authenticateWithClientInfo(request, sessionInfo, httpRequest);
        log.info("로그인 성공 - 이메일: {}", maskEmail(request.getEmail()));
        return ResponseEntity.ok(ApiResponse.success(UserControllerMessages.LOGIN_SUCCESS, response));
    }

    /**
     * 로그아웃 API
     * 클라이언트 정보를 포함한 강화된 로그아웃 처리
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<LogoutResponse>> logout(
            jakarta.servlet.http.HttpServletRequest httpRequest) {

        log.debug("로그아웃 요청 시작");
        // 인증 확인 및 사용자 ID 추출
        Long currentUserId = authService.requireAuthentication();

        log.info("로그아웃 진행 - 사용자 ID: {}", currentUserId);
        // 클라이언트 정보를 포함한 로그아웃 처리
        LogoutResponse logoutResponse = authService.logoutWithDetails(currentUserId, httpRequest);
        log.info("로그아웃 완료 - 사용자 ID: {}, 무효화된 세션: {}", currentUserId, logoutResponse.getInvalidatedSessions());

        return ResponseEntity.ok(ApiResponse.success(UserControllerMessages.LOGOUT_SUCCESS, logoutResponse));
    }

    /**
     * 토큰 갱신 API (보안 강화)
     * RefreshToken을 사용하여 새로운 AccessToken과 RefreshToken을 발급합니다.
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        log.info("토큰 갱신 요청 시작");

        // 추가 검증: 리프레시 토큰 형식 기본 검사
        if (request.getRefreshToken() == null || request.getRefreshToken().trim().isEmpty()) {
            log.warn("빈 리프레시 토큰으로 갱신 시도");
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("유효하지 않은 토큰입니다."));
        }

        try {
            // AuthService를 통해 토큰 갱신
            JwtAuthenticationResponse response = authService.refreshToken(request);

            log.info("토큰 갱신 성공");
            return ResponseEntity.ok(ApiResponse.success("토큰 갱신 성공", response));

        } catch (com.pickteam.exception.auth.InvalidTokenException e) {
            // 토큰 관련 예외 (만료, 무효, 변조 등)
            log.warn("유효하지 않은 토큰으로 갱신 시도: {}", e.getMessage());
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("토큰이 유효하지 않습니다. 다시 로그인해주세요."));

        } catch (com.pickteam.exception.validation.ValidationException e) {
            // 요청 검증 예외
            log.warn("잘못된 토큰 갱신 요청: {}", e.getMessage());
            return ResponseEntity.status(400)
                    .body(ApiResponse.error("잘못된 요청입니다."));

        } catch (Exception e) {
            // 기타 예상치 못한 예외
            log.error("토큰 갱신 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
        }
    }

    // ==================== 비밀번호 찾기 API ====================

    /**
     * 1. 비밀번호 재설정 이메일 발송 API
     * 등록된 이메일로 6자리 비밀번호 재설정 코드를 발송합니다.
     */
    @PostMapping("/send-password-reset-email")
    public ResponseEntity<ApiResponse<Void>> sendPasswordResetEmail(
            @Valid @RequestBody SendPasswordResetEmailRequest request) {

        log.info("비밀번호 재설정 이메일 발송 요청 - 이메일: {}", maskEmail(request.getEmail()));

        // 추가 검증: 이메일 형식 및 도메인 체크
        if (request.getEmail() != null) {
            if (isBlockedEmailDomain(request.getEmail())) {
                log.warn("차단된 도메인으로 비밀번호 재설정 요청: {}", maskEmail(request.getEmail()));
                throw new ValidationException("지원하지 않는 이메일 도메인입니다.");
            }
        }

        // 비밀번호 재설정 이메일 발송 (기존 이메일 인증 시스템 재활용)
        authService.sendPasswordResetEmail(request.getEmail());

        log.info("비밀번호 재설정 이메일 발송 완료 - 이메일: {}", maskEmail(request.getEmail()));

        // 보안상 이유로 존재하지 않는 이메일이어도 성공 메시지 반환
        return ResponseEntity.ok(ApiResponse.success("비밀번호 재설정 이메일이 발송되었습니다.", null));
    }

    /**
     * 2. 재설정 코드 확인 API
     * 이메일로 받은 6자리 재설정 코드를 확인합니다.
     */
    @PostMapping("/verify-reset-code")
    public ResponseEntity<ApiResponse<VerifyResetCodeResponse>> verifyResetCode(
            @Valid @RequestBody VerifyResetCodeRequest request) {

        log.info("비밀번호 재설정 코드 확인 요청 - 이메일: {}", maskEmail(request.getEmail()));

        // 추가 검증: 인증 코드 형식 체크
        if (request.getResetCode() != null && !isValidVerificationCode(request.getResetCode())) {
            log.warn("잘못된 재설정 코드 형식: 이메일={}", maskEmail(request.getEmail()));
            throw new ValidationException("유효하지 않은 재설정 코드 형식입니다.");
        }

        // 재설정 코드 검증 (기존 이메일 인증 시스템 재활용)
        boolean isValid = authService.verifyPasswordResetCode(request.getEmail(), request.getResetCode());

        VerifyResetCodeResponse response = new VerifyResetCodeResponse();
        response.setSuccess(true);
        response.setValid(isValid);
        response.setMessage(isValid ? "인증 코드가 확인되었습니다." : "인증 코드가 올바르지 않습니다.");

        log.info("비밀번호 재설정 코드 확인 결과 - 이메일: {}, 유효성: {}", maskEmail(request.getEmail()), isValid);
        return ResponseEntity.ok(ApiResponse.success("재설정 코드 확인 완료", response));
    }

    /**
     * 3. 새 비밀번호 설정 API
     * 인증된 재설정 코드로 새 비밀번호를 설정합니다.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        log.info("비밀번호 재설정 요청 - 이메일: {}", maskEmail(request.getEmail()));

        // 추가 검증: 재설정 코드 형식 체크
        if (request.getResetCode() != null && !isValidVerificationCode(request.getResetCode())) {
            log.warn("잘못된 재설정 코드 형식: 이메일={}", maskEmail(request.getEmail()));
            throw new ValidationException("유효하지 않은 재설정 코드 형식입니다.");
        }

        // 추가 검증: 새 비밀번호 유효성 체크
        if (request.getNewPassword() != null && request.getNewPassword().length() < 8) {
            log.warn("너무 짧은 새 비밀번호: 이메일={}", maskEmail(request.getEmail()));
            throw new ValidationException("새 비밀번호는 최소 8자 이상이어야 합니다.");
        }

        // 비밀번호 재설정 처리
        authService.resetPasswordWithCode(request.getEmail(), request.getResetCode(), request.getNewPassword());

        log.info("비밀번호 재설정 완료 - 이메일: {}", maskEmail(request.getEmail()));
        return ResponseEntity.ok(ApiResponse.success("비밀번호가 성공적으로 재설정되었습니다.", null));
    }

    // ==================== 이메일 인증 API ====================

    /**
     * 이메일 인증 요청 API
     * 회원가입 시 이메일 인증을 위한 코드를 발송합니다.
     */
    @PostMapping("/email/request")
    public ResponseEntity<ApiResponse<Void>> requestEmailVerification(
            @Valid @RequestBody EmailVerificationRequest request) {

        log.info("이메일 인증 요청 - 이메일: {}", maskEmail(request.getEmail()));

        // 추가 검증: 이메일 형식 및 도메인 체크
        if (request.getEmail() != null) {
            if (isBlockedEmailDomain(request.getEmail())) {
                log.warn("차단된 도메인으로 이메일 인증 요청: {}", maskEmail(request.getEmail()));
                throw new ValidationException("지원하지 않는 이메일 도메인입니다.");
            }
        }

        userService.requestEmailVerification(request.getEmail());
        log.info("이메일 인증 메일 발송 완료 - 이메일: {}", maskEmail(request.getEmail()));
        return ResponseEntity.ok(ApiResponse.success(UserControllerMessages.EMAIL_VERIFICATION_SENT, null));
    }

    /**
     * 이메일 인증 확인 API
     * 이메일로 받은 인증 코드를 확인합니다.
     */
    @PostMapping("/email/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyEmail(
            @Valid @RequestBody EmailVerificationConfirmRequest request) {

        log.info("이메일 인증 확인 요청 - 이메일: {}", maskEmail(request.getEmail()));

        // 추가 검증: 인증 코드 형식 체크
        if (request.getVerificationCode() != null && !isValidVerificationCode(request.getVerificationCode())) {
            log.warn("잘못된 인증 코드 형식: 이메일={}", maskEmail(request.getEmail()));
            throw new ValidationException("유효하지 않은 인증 코드 형식입니다.");
        }

        boolean isVerified = userService.verifyEmail(request.getEmail(), request.getVerificationCode());
        log.info("이메일 인증 확인 결과 - 이메일: {}, 인증성공: {}", maskEmail(request.getEmail()), isVerified);
        return ResponseEntity.ok(ApiResponse.success(UserControllerMessages.EMAIL_VERIFICATION_SUCCESS, isVerified));
    }

    // ==================== 유틸리티 메서드 ====================

    /**
     * 차단된 이메일 도메인 체크
     * - 임시 이메일 서비스나 알려진 스팸 도메인 차단
     * - 보안 강화를 위한 추가 검증
     */
    private boolean isBlockedEmailDomain(String email) {
        if (email == null || !email.contains("@")) {
            return false;
        }

        String domain = email.substring(email.lastIndexOf("@") + 1).toLowerCase();

        // 환경변수에서 차단 도메인 목록 읽어오기
        String[] blockedDomains = blockedDomainsConfig.split(",");

        for (String blockedDomain : blockedDomains) {
            if (domain.equals(blockedDomain.trim())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 인증 코드 형식 유효성 검증
     * - 일반적으로 6자리 숫자 형태의 인증 코드 검증
     */
    private boolean isValidVerificationCode(String verificationCode) {
        if (verificationCode == null || verificationCode.trim().isEmpty()) {
            return false;
        }

        // 6자리 숫자 형태의 인증 코드 검증
        String codeRegex = "^[0-9]{6}$";
        return verificationCode.matches(codeRegex);
    }

    /**
     * 이메일 마스킹 처리 (보안)
     */
    private String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "[EMPTY]";
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return "[INVALID_EMAIL]";
        }

        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        if (localPart.length() <= 2) {
            return "**" + domain;
        } else {
            return localPart.substring(0, 2) + "***" + domain;
        }
    }
}
