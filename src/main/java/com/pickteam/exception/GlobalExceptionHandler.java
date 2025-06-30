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

        Map<String, Object> extensions = new HashMap<>();
        extensions.put("fields", fieldErrors);
        extensions.put("timestamp", LocalDateTime.now());

        ProblemDetail problemDetail = ProblemDetail.builder()
                .type(ProblemType.VALIDATION_FAILED.getType())
                .title(ProblemType.VALIDATION_FAILED.getTitle())
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("입력값 검증에 실패했습니다.")
                .instance("/validation-error")
                .extensions(extensions)
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(problemDetail);
    }

    /**
     * 이메일 인증 실패 예외 처리
     */
    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ProblemDetail> handleEmailNotVerifiedException(EmailNotVerifiedException ex) {
        log.warn("이메일 인증 실패: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.builder()
                .type(ProblemType.EMAIL_NOT_VERIFIED.getType())
                .title(ProblemType.EMAIL_NOT_VERIFIED.getTitle())
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(ex.getMessage())
                .instance("/email-verification-error")
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(problemDetail);
    }

    /**
     * 사용자 조회 실패 예외 처리
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleUserNotFoundException(UserNotFoundException ex) {
        log.warn("사용자 조회 실패: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.builder()
                .type(ProblemType.USER_NOT_FOUND.getType())
                .title(ProblemType.USER_NOT_FOUND.getTitle())
                .status(HttpStatus.NOT_FOUND.value())
                .detail(ex.getMessage())
                .instance("/user-not-found")
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(problemDetail);
    }

    /**
     * 인증되지 않은 사용자 접근 예외 처리
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ProblemDetail> handleUnauthorizedException(UnauthorizedException ex) {
        log.warn("인증되지 않은 접근 시도: {}", ex.getMessage());

        Map<String, Object> extensions = new HashMap<>();
        extensions.put("errorCode", SessionErrorCode.SESSION_INVALID);
        extensions.put("timestamp", LocalDateTime.now());

        ProblemDetail problemDetail = ProblemDetail.builder()
                .type(ProblemType.UNAUTHORIZED_ACCESS.getType())
                .title(ProblemType.UNAUTHORIZED_ACCESS.getTitle())
                .status(HttpStatus.UNAUTHORIZED.value())
                .detail(ex.getMessage())
                .instance("/unauthorized-access")
                .extensions(extensions)
                .build();

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(problemDetail);
    }

    /**
     * 인증 예외 처리
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthenticationException(AuthenticationException ex) {
        log.warn("인증 실패: {}", ex.getMessage());

        Map<String, Object> extensions = new HashMap<>();
        extensions.put("errorCode", SessionErrorCode.LOGIN_FAILED);
        extensions.put("timestamp", LocalDateTime.now());

        ProblemDetail problemDetail = ProblemDetail.builder()
                .type(ProblemType.AUTHENTICATION_FAILED.getType())
                .title(ProblemType.AUTHENTICATION_FAILED.getTitle())
                .status(HttpStatus.UNAUTHORIZED.value())
                .detail(ex.getMessage())
                .instance("/authentication-failed")
                .extensions(extensions)
                .build();

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(problemDetail);
    }

    /**
     * 유효성 검사 예외 처리
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(ValidationException ex) {
        log.warn("유효성 검사 실패: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.builder()
                .type(ProblemType.BUSINESS_VALIDATION_FAILED.getType())
                .title(ProblemType.BUSINESS_VALIDATION_FAILED.getTitle())
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(ex.getMessage())
                .instance("/business-validation-error")
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(problemDetail);
    }

    /**
     * 이메일 중복 예외 처리
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ProblemDetail> handleDuplicateEmailException(DuplicateEmailException ex) {
        log.warn("이메일 중복: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.builder()
                .type(ProblemType.DUPLICATE_EMAIL.getType())
                .title(ProblemType.DUPLICATE_EMAIL.getTitle())
                .status(HttpStatus.CONFLICT.value())
                .detail(ex.getMessage())
                .instance("/duplicate-email")
                .build();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(problemDetail);
    }

    /**
     * 이메일 발송 예외 처리
     */
    @ExceptionHandler(EmailSendException.class)
    public ResponseEntity<ProblemDetail> handleEmailSendException(EmailSendException ex) {
        log.error("이메일 발송 실패: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.builder()
                .type(ProblemType.EMAIL_SEND_FAILED.getType())
                .title(ProblemType.EMAIL_SEND_FAILED.getTitle())
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .detail("이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.")
                .instance("/email-send-error")
                .build();

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(problemDetail);
    }

    /**
     * 잘못된 토큰 예외 처리
     */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ProblemDetail> handleInvalidTokenException(InvalidTokenException ex) {
        log.warn("잘못된 토큰: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.builder()
                .type(ProblemType.INVALID_TOKEN.getType())
                .title(ProblemType.INVALID_TOKEN.getTitle())
                .status(HttpStatus.UNAUTHORIZED.value())
                .detail(ex.getMessage())
                .instance("/invalid-token")
                .build();

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(problemDetail);
    }

    /**
     * 세션 만료 예외 처리
     */
    @ExceptionHandler(SessionExpiredException.class)
    public ResponseEntity<ProblemDetail> handleSessionExpiredException(SessionExpiredException ex) {
        log.warn("세션 만료: {}", ex.getMessage());

        Map<String, Object> extensions = new HashMap<>();
        extensions.put("errorCode", SessionErrorCode.DUPLICATE_LOGIN);
        extensions.put("timestamp", LocalDateTime.now());

        ProblemDetail problemDetail = ProblemDetail.builder()
                .type(ProblemType.SESSION_EXPIRED.getType())
                .title(ProblemType.SESSION_EXPIRED.getTitle())
                .status(HttpStatus.UNAUTHORIZED.value())
                .detail(ex.getMessage())
                .instance("/session-expired")
                .extensions(extensions)
                .build();

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(problemDetail);
    }

    /**
     * 계정 탈퇴 관련 예외 처리
     */
    @ExceptionHandler(AccountWithdrawalException.class)
    public ResponseEntity<ProblemDetail> handleAccountWithdrawalException(AccountWithdrawalException ex) {
        log.warn("탈퇴 계정 관련 오류: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.builder()
                .type(ProblemType.ACCOUNT_WITHDRAWAL_IN_PROGRESS.getType())
                .title(ProblemType.ACCOUNT_WITHDRAWAL_IN_PROGRESS.getTitle())
                .status(HttpStatus.CONFLICT.value())
                .detail(ex.getDetailedMessage())
                .instance("/account-withdrawal-error")
                .build();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(problemDetail);
    }

    /**
     * 잘못된 상태 예외 처리
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalStateException(IllegalStateException ex) {
        log.warn("잘못된 상태: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.builder()
                .type(ProblemType.ILLEGAL_STATE.getType())
                .title(ProblemType.ILLEGAL_STATE.getTitle())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail(ex.getMessage())
                .instance("/illegal-state")
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(problemDetail);
    }

    /**
     * 데이터 무결성 위반 예외 처리 (이메일 중복 등)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.warn("데이터 무결성 위반: {}", ex.getMessage());

        String errorMessage = "데이터 무결성 위반이 발생했습니다.";
        String problemType = ProblemType.DATA_INTEGRITY_VIOLATION.getType();
        String title = ProblemType.DATA_INTEGRITY_VIOLATION.getTitle();

        // 이메일 중복 관련 오류인지 확인
        if (ex.getMessage() != null && ex.getMessage().contains("email")) {
            errorMessage = "이미 사용 중인 이메일입니다. 탈퇴한 계정의 경우 완전 삭제 후 재사용 가능합니다.";
            problemType = ProblemType.DUPLICATE_EMAIL.getType();
            title = ProblemType.DUPLICATE_EMAIL.getTitle();
        }

        ProblemDetail problemDetail = ProblemDetail.builder()
                .type(problemType)
                .title(title)
                .status(HttpStatus.CONFLICT.value())
                .detail(errorMessage)
                .instance("/data-integrity-violation")
                .build();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(problemDetail);
    }

    /**
     * 일반적인 RuntimeException 처리
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ProblemDetail> handleRuntimeException(RuntimeException ex) {
        log.error("서버 오류 발생", ex);

        ProblemDetail problemDetail = ProblemDetail.builder()
                .type(ProblemType.INTERNAL_SERVER_ERROR.getType())
                .title(ProblemType.INTERNAL_SERVER_ERROR.getTitle())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail("서버 내부 오류가 발생했습니다.")
                .instance("/runtime-error")
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(problemDetail);
    }

    /**
     * 예상치 못한 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex) {
        log.error("예상치 못한 오류 발생", ex);

        ProblemDetail problemDetail = ProblemDetail.builder()
                .type(ProblemType.UNEXPECTED_ERROR.getType())
                .title(ProblemType.UNEXPECTED_ERROR.getTitle())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail("예상치 못한 오류가 발생했습니다.")
                .instance("/unexpected-error")
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(problemDetail);
    }
}
