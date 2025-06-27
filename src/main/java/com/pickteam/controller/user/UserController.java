package com.pickteam.controller.user;

import com.pickteam.dto.user.*;
import com.pickteam.dto.security.JwtAuthenticationResponse;
import com.pickteam.dto.ApiResponse;
import com.pickteam.domain.enums.UserRole;
import com.pickteam.service.user.UserService;
import com.pickteam.service.user.AuthService;
import com.pickteam.constants.UserControllerMessages;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> registerUser(@Valid @RequestBody UserRegisterRequest request) {
        log.info("회원가입 요청 - 이메일: {}", request.getEmail());

        // 추가 검증: 이메일 도메인 블랙리스트 체크 (보안 강화)
        if (request.getEmail() != null && isBlockedEmailDomain(request.getEmail())) {
            log.warn("차단된 이메일 도메인 회원가입 시도: {}", request.getEmail());
            throw new com.pickteam.exception.ValidationException("지원하지 않는 이메일 도메인입니다.");
        }

        userService.registerUser(request);
        log.info("회원가입 완료 - 이메일: {}", request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(UserControllerMessages.REGISTER_SUCCESS, null));
    }

    // ID 중복검사
    @PostMapping("/check-id")
    public ResponseEntity<ApiResponse<Boolean>> checkDuplicateId(@Valid @RequestBody CheckDuplicateIdRequest request) {
        log.debug("이메일 중복 검사 요청 - 이메일: {}", request.getEmail());
        boolean isDuplicate = userService.checkDuplicateId(request.getEmail());
        log.debug("이메일 중복 검사 결과 - 이메일: {}, 중복여부: {}", request.getEmail(), isDuplicate);
        return ResponseEntity.ok(ApiResponse.success(UserControllerMessages.CHECK_DUPLICATE_SUCCESS, !isDuplicate));
    }

    // 비밀번호 유효성 검사
    @PostMapping("/validate-password")
    public ResponseEntity<ApiResponse<Boolean>> validatePassword(@Valid @RequestBody ValidatePasswordRequest request) {
        log.debug("비밀번호 유효성 검사 요청");
        boolean isValid = userService.validatePassword(request.getPassword());
        log.debug("비밀번호 유효성 검사 결과: {}", isValid);
        return ResponseEntity.ok(ApiResponse.success(UserControllerMessages.PASSWORD_VALIDATION_SUCCESS, isValid));
    }

    // 메일 인증 요청
    @PostMapping("/email/request")
    public ResponseEntity<ApiResponse<Void>> requestEmailVerification(
            @Valid @RequestBody EmailVerificationRequest request) {
        log.info("이메일 인증 요청 - 이메일: {}", request.getEmail());

        // 추가 검증: 이메일 형식 및 도메인 체크
        if (request.getEmail() != null) {
            if (!isValidEmailFormat(request.getEmail())) {
                log.warn("잘못된 형식의 이메일로 인증 요청: {}", request.getEmail());
                throw new com.pickteam.exception.ValidationException("올바른 이메일 형식이 아닙니다.");
            }

            if (isBlockedEmailDomain(request.getEmail())) {
                log.warn("차단된 도메인으로 이메일 인증 요청: {}", request.getEmail());
                throw new com.pickteam.exception.ValidationException("지원하지 않는 이메일 도메인입니다.");
            }
        }

        userService.requestEmailVerification(request.getEmail());
        log.info("이메일 인증 메일 발송 완료 - 이메일: {}", request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(UserControllerMessages.EMAIL_VERIFICATION_SENT, null));
    }

    // 메일 인증 확인
    @PostMapping("/email/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyEmail(
            @Valid @RequestBody EmailVerificationConfirmRequest request) {
        log.info("이메일 인증 확인 요청 - 이메일: {}", request.getEmail());

        // 추가 검증: 인증 코드 형식 체크
        if (request.getVerificationCode() != null && !isValidVerificationCode(request.getVerificationCode())) {
            log.warn("잘못된 인증 코드 형식: 이메일={}", request.getEmail());
            throw new com.pickteam.exception.ValidationException("유효하지 않은 인증 코드 형식입니다.");
        }

        boolean isVerified = userService.verifyEmail(request.getEmail(), request.getVerificationCode());
        log.info("이메일 인증 확인 결과 - 이메일: {}, 인증성공: {}", request.getEmail(), isVerified);
        return ResponseEntity.ok(ApiResponse.success(UserControllerMessages.EMAIL_VERIFICATION_SUCCESS, isVerified));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> login(@Valid @RequestBody UserLoginRequest request) {
        log.info("로그인 시도 - 이메일: {}", request.getEmail());

        // 추가 검증: 이메일 형식 재확인 (이중 검증)
        if (request.getEmail() != null && !isValidEmailFormat(request.getEmail())) {
            log.warn("잘못된 이메일 형식으로 로그인 시도: {}", request.getEmail());
            throw new com.pickteam.exception.ValidationException("올바른 이메일 형식이 아닙니다.");
        }

        // 추가 검증: 비밀번호 최소 길이 체크 (보안 강화)
        if (request.getPassword() != null && request.getPassword().length() < 8) {
            log.warn("너무 짧은 비밀번호로 로그인 시도: 이메일={}", request.getEmail());
            throw new com.pickteam.exception.ValidationException("비밀번호는 최소 8자 이상이어야 합니다.");
        }

        JwtAuthenticationResponse response = userService.login(request);
        log.info("로그인 성공 - 이메일: {}", request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(UserControllerMessages.LOGIN_SUCCESS, response));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        log.debug("로그아웃 요청 시작");
        // 인증 확인 및 사용자 ID 추출
        Long currentUserId = authService.requireAuthentication();

        log.info("로그아웃 진행 - 사용자 ID: {}", currentUserId);
        // Refresh Token 무효화
        authService.logout(currentUserId);
        log.info("로그아웃 완료 - 사용자 ID: {}", currentUserId);

        return ResponseEntity.ok(ApiResponse.success(UserControllerMessages.LOGOUT_SUCCESS, null));
    }

    // 내 프로필 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile() {
        log.debug("내 프로필 조회 요청");
        // 인증 확인 및 사용자 ID 추출
        Long currentUserId = authService.requireAuthentication();

        log.debug("내 프로필 조회 - 사용자 ID: {}", currentUserId);
        UserProfileResponse profile = userService.getMyProfile(currentUserId);
        log.info("내 프로필 조회 완료 - 사용자 ID: {}", currentUserId);
        return ResponseEntity.ok(ApiResponse.success(UserControllerMessages.PROFILE_GET_SUCCESS, profile));
    }

    // 내 프로필 수정
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<Void>> updateMyProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        log.debug("내 프로필 수정 요청");
        // 인증 확인 및 사용자 ID 추출
        Long currentUserId = authService.requireAuthentication();

        log.info("내 프로필 수정 시작 - 사용자 ID: {}", currentUserId);
        userService.updateMyProfile(currentUserId, request);
        log.info("내 프로필 수정 완료 - 사용자 ID: {}", currentUserId);
        return ResponseEntity.ok(ApiResponse.success(UserControllerMessages.PROFILE_UPDATE_SUCCESS, null));
    }

    // 다른 사람의 프로필 조회
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(
            @PathVariable @Positive(message = "사용자 ID는 양수여야 합니다.") Long userId) {
        log.debug("다른 사용자 프로필 조회 요청 - 대상 사용자 ID: {}", userId);

        // 추가 검증: 사용자 ID 범위 체크 (보안 강화)
        if (userId > Long.MAX_VALUE / 2) {
            log.warn("비정상적인 사용자 ID 접근 시도: {}", userId);
            throw new com.pickteam.exception.ValidationException("유효하지 않은 사용자 ID입니다.");
        }

        UserProfileResponse profile = userService.getUserProfile(userId);
        log.debug("다른 사용자 프로필 조회 완료 - 대상 사용자 ID: {}", userId);
        return ResponseEntity.ok(ApiResponse.success(UserControllerMessages.USER_PROFILE_GET_SUCCESS, profile));
    }

    // 전체 사용자 프로필 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> getAllUserProfile() {
        log.debug("전체 사용자 프로필 조회 요청");
        List<UserProfileResponse> profiles = userService.getAllUserProfile();
        log.info("전체 사용자 프로필 조회 완료 - 조회된 사용자 수: {}", profiles.size());
        return ResponseEntity.ok(ApiResponse.success(UserControllerMessages.ALL_USER_PROFILE_GET_SUCCESS, profiles));
    }

    // 추천 팀원 리스트 조회
    @GetMapping("/recommend")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> getRecommendedTeamMembers() {
        log.debug("추천 팀원 조회 요청");
        // 인증 확인 및 사용자 ID 추출
        Long currentUserId = authService.requireAuthentication();

        log.debug("추천 팀원 조회 - 사용자 ID: {}", currentUserId);
        List<UserProfileResponse> recommendedMembers = userService.getRecommendedTeamMembers(currentUserId);
        log.info("추천 팀원 조회 완료 - 사용자 ID: {}, 추천된 팀원 수: {}", currentUserId, recommendedMembers.size());
        return ResponseEntity
                .ok(ApiResponse.success(UserControllerMessages.RECOMMENDED_MEMBERS_GET_SUCCESS, recommendedMembers));
    }

    // 비밀번호 변경
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.debug("비밀번호 변경 요청");
        // 인증 확인 및 사용자 ID 추출
        Long currentUserId = authService.requireAuthentication();

        // 추가 검증: 새 비밀번호와 기존 비밀번호가 동일한지 체크
        if (request.getCurrentPassword() != null && request.getNewPassword() != null &&
                request.getCurrentPassword().equals(request.getNewPassword())) {
            log.warn("동일한 비밀번호로 변경 시도 - 사용자 ID: {}", currentUserId);
            throw new com.pickteam.exception.ValidationException("새 비밀번호는 기존 비밀번호와 달라야 합니다.");
        }

        log.info("비밀번호 변경 시작 - 사용자 ID: {}", currentUserId);
        userService.changePassword(currentUserId, request);
        log.info("비밀번호 변경 완료 - 사용자 ID: {}", currentUserId);
        return ResponseEntity.ok(ApiResponse.success(UserControllerMessages.PASSWORD_CHANGE_SUCCESS, null));
    }

    // 계정 삭제
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteAccount() {
        log.debug("계정 삭제 요청");
        // 인증 확인 및 사용자 ID 추출
        Long currentUserId = authService.requireAuthentication();

        // 추가 검증: 관리자 계정 보호
        UserProfileResponse userProfile = userService.getMyProfile(currentUserId);
        if (userProfile.getRole() == UserRole.ADMIN) {
            log.error("관리자 계정 삭제 시도 - 사용자 ID: {}", currentUserId);
            throw new com.pickteam.exception.ValidationException("관리자 계정은 삭제할 수 없습니다.");
        }

        log.warn("계정 삭제 시작 - 사용자 ID: {} (중요: 계정 삭제 작업)", currentUserId);
        userService.deleteAccount(currentUserId);
        log.warn("계정 삭제 완료 - 사용자 ID: {} (중요: 계정 삭제 완료)", currentUserId);
        return ResponseEntity.ok(ApiResponse.success(UserControllerMessages.ACCOUNT_DELETE_SUCCESS, null));
    }

    /**
     * 차단된 이메일 도메인 체크
     * - 임시 이메일 서비스나 알려진 스팸 도메인 차단
     * - 보안 강화를 위한 추가 검증
     * 
     * @param email 검증할 이메일 주소
     * @return 차단된 도메인이면 true, 그렇지 않으면 false
     */
    private boolean isBlockedEmailDomain(String email) {
        if (email == null || !email.contains("@")) {
            return false;
        }

        String domain = email.substring(email.lastIndexOf("@") + 1).toLowerCase();

        // 일반적인 임시 이메일 서비스 도메인들
        String[] blockedDomains = {
                "10minutemail.com", "guerrillamail.com", "mailinator.com",
                "tempmail.org", "throwaway.email", "example.com", "test.com"
        };

        for (String blockedDomain : blockedDomains) {
            if (domain.equals(blockedDomain)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 이메일 형식 유효성 검증
     * - 기본적인 이메일 형식 체크
     * - @Valid 애노테이션과 함께 이중 검증
     * 
     * @param email 검증할 이메일 주소
     * @return 유효한 형식이면 true, 그렇지 않으면 false
     */
    private boolean isValidEmailFormat(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        // 기본적인 이메일 형식 체크
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * 인증 코드 형식 유효성 검증
     * - 일반적으로 6자리 숫자 형태의 인증 코드 검증
     * - 보안을 위한 추가 형식 체크
     * 
     * @param verificationCode 검증할 인증 코드
     * @return 유효한 형식이면 true, 그렇지 않으면 false
     */
    private boolean isValidVerificationCode(String verificationCode) {
        if (verificationCode == null || verificationCode.trim().isEmpty()) {
            return false;
        }

        // 6자리 숫자 형태의 인증 코드 검증
        String codeRegex = "^[0-9]{6}$";
        return verificationCode.matches(codeRegex);
    }

    // ==================== 예외 처리 메서드들 ====================

    /**
     * HttpMessageNotReadableException 처리
     * - 요청 본문이 누락되거나 형식이 잘못된 경우 발생
     * - JSON 파싱 오류, 필수 Request Body 누락 등
     * - 400 Bad Request로 응답
     */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
            org.springframework.http.converter.HttpMessageNotReadableException ex) {
        String errorMessage = ex.getMessage();

        // 요청 본문 누락인 경우
        if (errorMessage != null && errorMessage.contains("Required request body is missing")) {
            log.warn("요청 본문 누락: {}", errorMessage);
            ApiResponse<Void> response = ApiResponse.error("요청 본문이 필요합니다. JSON 데이터를 포함해서 요청해주세요.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // JSON 파싱 오류인 경우
        if (errorMessage != null
                && (errorMessage.contains("JSON parse error") || errorMessage.contains("not well-formed"))) {
            log.warn("JSON 파싱 오류: {}", errorMessage);
            ApiResponse<Void> response = ApiResponse.error("잘못된 JSON 형식입니다. 요청 데이터를 확인해주세요.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // 기타 메시지 읽기 오류
        log.warn("요청 메시지 읽기 오류: {}", errorMessage);
        ApiResponse<Void> response = ApiResponse.error("요청 데이터 형식이 올바르지 않습니다.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * IllegalArgumentException 처리
     * - 입력값 검증 실패 시 발생하는 예외
     * - 400 Bad Request로 응답
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("잘못된 요청 파라미터: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * MethodArgumentNotValidException 처리
     * - @Valid 검증 실패 시 발생하는 예외
     * - 400 Bad Request로 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        StringBuilder errorMessage = new StringBuilder("입력값 검증 실패: ");

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errorMessage.append(error.getField())
                    .append(" - ")
                    .append(error.getDefaultMessage())
                    .append("; ");
        }

        String message = errorMessage.toString();
        log.warn("입력값 검증 실패: {}", message);

        ApiResponse<Void> response = ApiResponse.error(message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * jakarta.validation.ConstraintViolationException 처리
     * - @Positive 등 제약 조건 위반 시 발생하는 예외
     * - 400 Bad Request로 응답
     */
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
            jakarta.validation.ConstraintViolationException ex) {
        StringBuilder errorMessage = new StringBuilder("제약 조건 위반: ");

        ex.getConstraintViolations().forEach(violation -> {
            errorMessage.append(violation.getPropertyPath())
                    .append(" - ")
                    .append(violation.getMessage())
                    .append("; ");
        });

        String message = errorMessage.toString();
        log.warn("제약 조건 위반: {}", message);

        ApiResponse<Void> response = ApiResponse.error(message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 커스텀 예외들 처리 (UnauthorizedException 등)
     * - 인증/권한 관련 예외
     * - 401 Unauthorized로 응답
     */
    @ExceptionHandler(com.pickteam.exception.UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedException(
            com.pickteam.exception.UnauthorizedException ex) {
        log.warn("인증 실패: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * UserNotFoundException 처리
     * - 사용자를 찾을 수 없는 경우
     * - 404 Not Found로 응답
     */
    @ExceptionHandler(com.pickteam.exception.UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFoundException(
            com.pickteam.exception.UserNotFoundException ex) {
        log.warn("사용자 없음: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * AuthenticationException 처리
     * - 로그인 실패 등 인증 실패
     * - 401 Unauthorized로 응답
     */
    @ExceptionHandler(com.pickteam.exception.AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            com.pickteam.exception.AuthenticationException ex) {
        log.warn("인증 실패: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * ValidationException 처리
     * - 비즈니스 로직 검증 실패
     * - 400 Bad Request로 응답
     */
    @ExceptionHandler(com.pickteam.exception.ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            com.pickteam.exception.ValidationException ex) {
        log.warn("비즈니스 검증 실패: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * DuplicateEmailException 처리
     * - 이메일 중복 시 발생
     * - 409 Conflict로 응답
     */
    @ExceptionHandler(com.pickteam.exception.DuplicateEmailException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateEmailException(
            com.pickteam.exception.DuplicateEmailException ex) {
        log.warn("이메일 중복: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * InvalidTokenException 처리
     * - 토큰이 유효하지 않은 경우
     * - 401 Unauthorized로 응답
     */
    @ExceptionHandler(com.pickteam.exception.InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidTokenException(
            com.pickteam.exception.InvalidTokenException ex) {
        log.warn("유효하지 않은 토큰: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * EmailSendException 처리
     * - 이메일 발송 실패
     * - 503 Service Unavailable로 응답
     */
    @ExceptionHandler(com.pickteam.exception.EmailSendException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailSendException(
            com.pickteam.exception.EmailSendException ex) {
        log.error("이메일 발송 실패: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error("이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * EmailNotVerifiedException 처리
     * - 이메일 인증이 완료되지 않은 경우
     * - 403 Forbidden으로 응답
     */
    @ExceptionHandler(com.pickteam.exception.EmailNotVerifiedException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailNotVerifiedException(
            com.pickteam.exception.EmailNotVerifiedException ex) {
        log.warn("이메일 미인증: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * PessimisticLockingFailureException 처리
     * - 데이터베이스 락 대기 시간 초과 시 발생
     * - 동시성 문제로 인한 락 타임아웃
     * - 503 Service Unavailable로 응답
     */
    @ExceptionHandler(org.springframework.dao.PessimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handlePessimisticLockingFailureException(
            org.springframework.dao.PessimisticLockingFailureException ex) {
        log.error("데이터베이스 락 타임아웃 발생: {}", ex.getMessage());
        log.warn("동시 요청으로 인한 락 대기 시간 초과 - 잠시 후 다시 시도하도록 안내");

        ApiResponse<Void> response = ApiResponse.error("현재 요청이 처리 중입니다. 잠시 후 다시 시도해주세요.");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * DataIntegrityViolationException 처리
     * - 데이터베이스 제약 조건 위반 시 발생
     * - 이메일 인증 중복 요청, 중복 이메일 등의 경우
     * - 409 Conflict로 응답
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String errorMessage = ex.getMessage();
        String rootCauseMessage = "";

        // 근본 원인 메시지 추출
        Throwable rootCause = ex.getRootCause();
        if (rootCause != null) {
            rootCauseMessage = rootCause.getMessage();
        }

        // 상세한 로그 출력 (디버깅용)
        log.error("=== DataIntegrityViolationException 상세 정보 ===");
        log.error("Exception Message: {}", errorMessage);
        log.error("Root Cause Message: {}", rootCauseMessage);
        log.error("Full Stack Trace: ", ex);

        // 이메일 인증 중복 요청인 경우 사용자 친화적 메시지 제공
        if ((errorMessage != null && errorMessage.contains("email_verification")
                && errorMessage.contains("Duplicate entry"))
                || (rootCauseMessage != null && rootCauseMessage.contains("email_verification")
                        && rootCauseMessage.contains("Duplicate entry"))) {
            log.warn("이메일 인증 중복 요청 감지: errorMessage={}, rootCause={}", errorMessage, rootCauseMessage);
            ApiResponse<Void> response = ApiResponse.error("이미 해당 이메일로 인증 요청이 진행 중입니다. 기존 인증을 완료하거나 잠시 후 다시 시도해주세요.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        // accounts 테이블 이메일 중복인 경우
        if ((errorMessage != null && errorMessage.contains("accounts") && errorMessage.contains("email"))
                || (rootCauseMessage != null && rootCauseMessage.contains("accounts")
                        && rootCauseMessage.contains("email"))) {
            log.warn("accounts 테이블 이메일 중복: errorMessage={}, rootCause={}", errorMessage, rootCauseMessage);
            ApiResponse<Void> response = ApiResponse.error("이미 사용 중인 이메일입니다. 다른 이메일을 사용해주세요.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        // 기타 제약 조건 위반
        log.error("알 수 없는 데이터베이스 제약 조건 위반: errorMessage={}, rootCause={}", errorMessage, rootCauseMessage);
        ApiResponse<Void> response = ApiResponse.error("데이터베이스 제약 조건 위반이 발생했습니다. 잠시 후 다시 시도해주세요.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * 기타 모든 예외 처리
     * - 예상하지 못한 예외들에 대한 fallback
     * - 500 Internal Server Error로 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("예상하지 못한 오류 발생", ex);
        ApiResponse<Void> response = ApiResponse.error("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
