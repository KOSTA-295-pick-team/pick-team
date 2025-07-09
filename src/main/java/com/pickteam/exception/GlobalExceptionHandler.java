package com.pickteam.exception;

import com.pickteam.constants.SessionErrorCode;
import com.pickteam.exception.email.EmailNotVerifiedException;
import com.pickteam.exception.email.EmailSendException;
import com.pickteam.exception.user.UserNotFoundException;
import com.pickteam.exception.user.DuplicateEmailException;
import com.pickteam.exception.user.AccountWithdrawalException;
import com.pickteam.exception.auth.AuthenticationException;
import com.pickteam.exception.auth.UnauthorizedException;
import com.pickteam.exception.auth.InvalidTokenException;
import com.pickteam.exception.auth.SessionExpiredException;
import com.pickteam.exception.validation.ValidationException;
import com.pickteam.exception.common.ProblemDetail;
import com.pickteam.exception.common.ProblemType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * RFC 9457 표준 글로벌 예외 핸들러
 * - RFC 9457 Problem Details for HTTP APIs 표준 준수
 * - 모든 예외를 ProblemDetail 형식으로 통일
 * - 사용자 친화적인 한국어 오류 메시지 제공
 * - 확장 가능한 구조적 오류 응답
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ===== 상수 정의 =====
    private static final String VALIDATION_ERROR_INSTANCE = "/validation-error";
    private static final String EMAIL_VERIFICATION_ERROR_INSTANCE = "/email-verification-error";
    private static final String USER_NOT_FOUND_INSTANCE = "/user-not-found";
    private static final String UNAUTHORIZED_ACCESS_INSTANCE = "/unauthorized-access";
    private static final String AUTHENTICATION_FAILED_INSTANCE = "/authentication-failed";
    private static final String BUSINESS_VALIDATION_ERROR_INSTANCE = "/business-validation-error";
    private static final String DUPLICATE_EMAIL_INSTANCE = "/duplicate-email";
    private static final String EMAIL_SEND_ERROR_INSTANCE = "/email-send-error";
    private static final String INVALID_TOKEN_INSTANCE = "/invalid-token";
    private static final String SESSION_EXPIRED_INSTANCE = "/session-expired";
    private static final String ACCOUNT_WITHDRAWAL_ERROR_INSTANCE = "/account-withdrawal-error";
    private static final String ILLEGAL_STATE_INSTANCE = "/illegal-state";
    private static final String DATA_INTEGRITY_VIOLATION_INSTANCE = "/data-integrity-violation";
    private static final String RUNTIME_ERROR_INSTANCE = "/runtime-error";
    private static final String UNEXPECTED_ERROR_INSTANCE = "/unexpected-error";
    private static final String MESSAGE_NOT_READABLE_INSTANCE = "/message-not-readable";
    private static final String ILLEGAL_ARGUMENT_INSTANCE = "/illegal-argument";
    private static final String CONSTRAINT_VIOLATION_INSTANCE = "/constraint-violation";
    private static final String DATABASE_LOCK_TIMEOUT_INSTANCE = "/database-lock-timeout";

    /**
     * ProblemDetail 생성 헬퍼 메서드
     * - 중복 코드 제거
     * - 일관된 응답 구조 보장
     */
    private ProblemDetail createProblemDetail(ProblemType problemType, HttpStatus status,
            String detail, String instance) {
        return createProblemDetail(problemType, status, detail, instance, null);
    }

    /**
     * ProblemDetail 생성 헬퍼 메서드 (확장 필드 포함)
     */
    private ProblemDetail createProblemDetail(ProblemType problemType, HttpStatus status,
            String detail, String instance, Map<String, Object> extensions) {
        ProblemDetail.ProblemDetailBuilder builder = ProblemDetail.builder()
                .type(problemType.getType())
                .title(problemType.getTitle())
                .status(status.value())
                .detail(detail)
                .instance(instance);

        if (extensions != null && !extensions.isEmpty()) {
            builder.extensions(extensions);
        }

        return builder.build();
    }

    /**
     * 기본 타임스탬프 확장 필드 생성
     */
    private Map<String, Object> createTimestampExtensions() {
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("timestamp", LocalDateTime.now());
        return extensions;
    }

    /**
     * HttpMessageNotReadableException 메시지 분석 헬퍼
     */
    private String analyzeHttpMessageError(String errorMessage) {
        if (errorMessage == null) {
            return "요청 데이터 형식이 올바르지 않습니다.";
        }

        // 요청 본문 누락인 경우
        if (errorMessage.contains("Required request body is missing")) {
            return "요청 본문이 필요합니다. JSON 데이터를 포함해서 요청해주세요.";
        }
        // JSON 파싱 오류인 경우
        else if (errorMessage.contains("JSON parse error") || errorMessage.contains("not well-formed")) {
            return "잘못된 JSON 형식입니다. 요청 데이터를 확인해주세요.";
        }

        return "요청 데이터 형식이 올바르지 않습니다.";
    }

    /**
     * ConstraintViolationException 메시지 구성 헬퍼
     */
    private String buildConstraintViolationMessage(jakarta.validation.ConstraintViolationException ex) {
        StringBuilder errorMessage = new StringBuilder("제약 조건 위반: ");
        ex.getConstraintViolations().forEach(violation -> {
            errorMessage.append(violation.getPropertyPath())
                    .append(" - ")
                    .append(violation.getMessage())
                    .append("; ");
        });
        return errorMessage.toString();
    }

    /**
     * Bean Validation 실패 시 처리
     * - @Valid 애노테이션으로 인한 검증 실패 처리
     * - 필드별 상세 오류 메시지 제공
     *
     * @param ex MethodArgumentNotValidException
     * @return RFC 9457 표준 검증 실패 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();

        // 모든 필드 오류를 수집
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        log.warn("검증 실패: {}", fieldErrors);

        Map<String, Object> extensions = createTimestampExtensions();
        extensions.put("fields", fieldErrors);

        ProblemDetail problemDetail = createProblemDetail(
                ProblemType.VALIDATION_FAILED,
                HttpStatus.BAD_REQUEST,
                "입력값 검증에 실패했습니다.",
                VALIDATION_ERROR_INSTANCE,
                extensions);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    /**
     * 이메일 인증 실패 예외 처리
     */
    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ProblemDetail> handleEmailNotVerifiedException(EmailNotVerifiedException ex) {
        log.warn("이메일 인증 실패: {}", ex.getMessage());

        ProblemDetail problemDetail = createProblemDetail(
                ProblemType.EMAIL_NOT_VERIFIED,
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                EMAIL_VERIFICATION_ERROR_INSTANCE);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    /**
     * 사용자 조회 실패 예외 처리
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleUserNotFoundException(UserNotFoundException ex) {
        log.warn("사용자 조회 실패: {}", ex.getMessage());

        ProblemDetail problemDetail = createProblemDetail(
                ProblemType.USER_NOT_FOUND,
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                USER_NOT_FOUND_INSTANCE);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    /**
     * 인증되지 않은 사용자 접근 예외 처리
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ProblemDetail> handleUnauthorizedException(UnauthorizedException ex) {
        log.warn("인증되지 않은 접근 시도: {}", ex.getMessage());

        Map<String, Object> extensions = createTimestampExtensions();
        extensions.put("errorCode", SessionErrorCode.SESSION_INVALID);

        ProblemDetail problemDetail = createProblemDetail(
                ProblemType.UNAUTHORIZED_ACCESS,
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                UNAUTHORIZED_ACCESS_INSTANCE,
                extensions);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }

    /**
     * 인증 예외 처리
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthenticationException(AuthenticationException ex) {
        log.warn("인증 실패: {}", ex.getMessage());

        Map<String, Object> extensions = createTimestampExtensions();
        extensions.put("errorCode", SessionErrorCode.LOGIN_FAILED);

        ProblemDetail problemDetail = createProblemDetail(
                ProblemType.AUTHENTICATION_FAILED,
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                AUTHENTICATION_FAILED_INSTANCE,
                extensions);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }

    /**
     * 유효성 검사 예외 처리
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(ValidationException ex) {
        log.warn("유효성 검사 실패: {}", ex.getMessage());

        ProblemDetail problemDetail = createProblemDetail(
                ProblemType.BUSINESS_VALIDATION_FAILED,
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                BUSINESS_VALIDATION_ERROR_INSTANCE);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    /**
     * 이메일 중복 예외 처리
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ProblemDetail> handleDuplicateEmailException(DuplicateEmailException ex) {
        log.warn("이메일 중복: {}", ex.getMessage());

        ProblemDetail problemDetail = createProblemDetail(
                ProblemType.DUPLICATE_EMAIL,
                HttpStatus.CONFLICT,
                ex.getMessage(),
                DUPLICATE_EMAIL_INSTANCE);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    /**
     * 이메일 발송 예외 처리
     */
    @ExceptionHandler(EmailSendException.class)
    public ResponseEntity<ProblemDetail> handleEmailSendException(EmailSendException ex) {
        log.error("이메일 발송 실패: {}", ex.getMessage());

        ProblemDetail problemDetail = createProblemDetail(
                ProblemType.EMAIL_SEND_FAILED,
                HttpStatus.SERVICE_UNAVAILABLE,
                "이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.",
                EMAIL_SEND_ERROR_INSTANCE);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problemDetail);
    }

    /**
     * 잘못된 토큰 예외 처리
     */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ProblemDetail> handleInvalidTokenException(InvalidTokenException ex) {
        log.warn("잘못된 토큰: {}", ex.getMessage());

        ProblemDetail problemDetail = createProblemDetail(
                ProblemType.INVALID_TOKEN,
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                INVALID_TOKEN_INSTANCE);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }

    /**
     * 세션 만료 예외 처리
     */
    @ExceptionHandler(SessionExpiredException.class)
    public ResponseEntity<ProblemDetail> handleSessionExpiredException(SessionExpiredException ex) {
        log.warn("세션 만료: {}", ex.getMessage());

        Map<String, Object> extensions = createTimestampExtensions();
        extensions.put("errorCode", SessionErrorCode.DUPLICATE_LOGIN);

        ProblemDetail problemDetail = createProblemDetail(
                ProblemType.SESSION_EXPIRED,
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                SESSION_EXPIRED_INSTANCE,
                extensions);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }

    /**
     * 계정 탈퇴 관련 예외 처리
     */
    @ExceptionHandler(AccountWithdrawalException.class)
    public ResponseEntity<ProblemDetail> handleAccountWithdrawalException(AccountWithdrawalException ex) {
        log.warn("탈퇴 계정 관련 오류: {}", ex.getMessage());

        ProblemDetail problemDetail = createProblemDetail(
                ProblemType.ACCOUNT_WITHDRAWAL_IN_PROGRESS,
                HttpStatus.CONFLICT,
                ex.getDetailedMessage(),
                ACCOUNT_WITHDRAWAL_ERROR_INSTANCE);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    /**
     * 잘못된 상태 예외 처리
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalStateException(IllegalStateException ex) {
        log.warn("잘못된 상태: {}", ex.getMessage());

        ProblemDetail problemDetail = createProblemDetail(
                ProblemType.ILLEGAL_STATE,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage(),
                ILLEGAL_STATE_INSTANCE);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    /**
     * 데이터 무결성 위반 예외 처리 (이메일 중복 등)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.warn("데이터 무결성 위반: {}", ex.getMessage());

        // 이메일 중복 관련 오류인지 확인
        if (ex.getMessage() != null && ex.getMessage().contains("email")) {
            ProblemDetail problemDetail = createProblemDetail(
                    ProblemType.DUPLICATE_EMAIL,
                    HttpStatus.CONFLICT,
                    "이미 사용 중인 이메일입니다. 탈퇴한 계정의 경우 완전 삭제 후 재사용 가능합니다.",
                    DUPLICATE_EMAIL_INSTANCE);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
        }

        ProblemDetail problemDetail = createProblemDetail(
                ProblemType.DATA_INTEGRITY_VIOLATION,
                HttpStatus.CONFLICT,
                "데이터 무결성 위반이 발생했습니다.",
                DATA_INTEGRITY_VIOLATION_INSTANCE);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    /**
     * 일반적인 RuntimeException 처리
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ProblemDetail> handleRuntimeException(RuntimeException ex) {
        log.error("서버 오류 발생", ex);

        ProblemDetail problemDetail = createProblemDetail(
                ProblemType.INTERNAL_SERVER_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "서버 내부 오류가 발생했습니다.",
                RUNTIME_ERROR_INSTANCE);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    /**
     * 예상치 못한 모든 예외 처리 (더 상세한 로깅)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex) {
        // 상세한 로깅으로 원인 파악 개선
        log.error("예상치 못한 오류 발생 - 예외 클래스: {}, 메시지: {}",
                ex.getClass().getSimpleName(), ex.getMessage(), ex);

        // 스택트레이스의 첫 번째 요소도 로깅
        if (ex.getStackTrace().length > 0) {
            StackTraceElement firstElement = ex.getStackTrace()[0];
            log.error("오류 발생 위치: {}:{}:{}",
                    firstElement.getClassName(),
                    firstElement.getMethodName(),
                    firstElement.getLineNumber());
        }

        ProblemDetail problemDetail = createProblemDetail(
                ProblemType.UNEXPECTED_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "예상치 못한 오류가 발생했습니다.",
                UNEXPECTED_ERROR_INSTANCE);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    /**
     * HttpMessageNotReadableException 처리
     * - 요청 본문이 누락되거나 형식이 잘못된 경우 발생
     * - JSON 파싱 오류, 필수 Request Body 누락 등
     */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadableException(
            org.springframework.http.converter.HttpMessageNotReadableException ex) {

        String errorMessage = ex.getMessage();
        String detail = analyzeHttpMessageError(errorMessage);

        log.warn("요청 메시지 읽기 오류: {}", errorMessage);

        ProblemDetail problemDetail = createProblemDetail(
                ProblemType.VALIDATION_FAILED,
                HttpStatus.BAD_REQUEST,
                detail,
                MESSAGE_NOT_READABLE_INSTANCE);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    /**
     * IllegalArgumentException 처리
     * - 입력값 검증 실패 시 발생하는 예외
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("잘못된 요청 파라미터: {}", ex.getMessage());

        ProblemDetail problemDetail = createProblemDetail(
                ProblemType.VALIDATION_FAILED,
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                ILLEGAL_ARGUMENT_INSTANCE);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    /**
     * ConstraintViolationException 처리
     * - @Positive 등 제약 조건 위반 시 발생하는 예외
     */
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolationException(
            jakarta.validation.ConstraintViolationException ex) {

        String message = buildConstraintViolationMessage(ex);
        log.warn("제약 조건 위반: {}", message);

        Map<String, Object> extensions = createTimestampExtensions();
        extensions.put("violations", ex.getConstraintViolations().size());

        ProblemDetail problemDetail = createProblemDetail(
                ProblemType.CONSTRAINT_VIOLATION,
                HttpStatus.BAD_REQUEST,
                message,
                CONSTRAINT_VIOLATION_INSTANCE,
                extensions);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    /**
     * PessimisticLockingFailureException 처리
     * - 데이터베이스 락 대기 시간 초과 시 발생
     * - 동시성 문제로 인한 락 타임아웃
     */
    @ExceptionHandler(org.springframework.dao.PessimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetail> handlePessimisticLockingFailureException(
            org.springframework.dao.PessimisticLockingFailureException ex) {

        log.error("데이터베이스 락 타임아웃 발생: {}", ex.getMessage());
        log.warn("동시 요청으로 인한 락 대기 시간 초과 - 잠시 후 다시 시도하도록 안내");

        ProblemDetail problemDetail = createProblemDetail(
                ProblemType.SERVICE_UNAVAILABLE,
                HttpStatus.SERVICE_UNAVAILABLE,
                "현재 요청이 처리 중입니다. 잠시 후 다시 시도해주세요.",
                DATABASE_LOCK_TIMEOUT_INSTANCE);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problemDetail);
    }

    /**
     * NoResourceFoundException 처리 (Spring 6.0+)
     * - 정적 리소스가 존재하지 않을 때 발생
     * - favicon.ico, robots.txt 등의 404 에러 처리
     */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoResourceFoundException(
            org.springframework.web.servlet.resource.NoResourceFoundException ex) {

        // 정적 리소스(favicon, robots.txt 등)의 경우 로그 레벨을 낮춤
        String resourcePath = ex.getResourcePath();
        if (resourcePath != null && (resourcePath.contains("favicon") ||
                resourcePath.contains("robots.txt") ||
                resourcePath.contains(".well-known"))) {
            log.debug("정적 리소스 미존재: {}", resourcePath);
        } else {
            log.warn("리소스를 찾을 수 없음: {}", resourcePath);
        }

        ProblemDetail problemDetail = createProblemDetail(
                ProblemType.NOT_FOUND,
                HttpStatus.NOT_FOUND,
                "요청하신 리소스를 찾을 수 없습니다.",
                "/resource-not-found");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }
}
