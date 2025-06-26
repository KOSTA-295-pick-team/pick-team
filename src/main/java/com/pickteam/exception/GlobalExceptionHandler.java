package com.pickteam.exception;

import com.pickteam.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 글로벌 예외 핸들러
 * - 애플리케이션 전역에서 발생하는 예외를 일관되게 처리
 * - Bean Validation 실패, 사용자 정의 예외 등을 처리
 * - 사용자 친화적인 오류 메시지 제공
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
     * @return 검증 실패 필드와 메시지가 포함된 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        // 모든 필드 오류를 수집
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("검증 실패: {}", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("입력값 검증에 실패했습니다."));
    }

    /**
     * 이메일 인증 실패 예외 처리
     * 
     * @param ex EmailNotVerifiedException
     * @return 이메일 인증 실패 응답
     */
    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailNotVerifiedException(EmailNotVerifiedException ex) {
        log.warn("이메일 인증 실패: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * 사용자 조회 실패 예외 처리
     * 
     * @param ex UserNotFoundException
     * @return 사용자 조회 실패 응답
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFoundException(UserNotFoundException ex) {
        log.warn("사용자 조회 실패: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * 인증되지 않은 사용자 접근 예외 처리
     * 
     * @param ex UnauthorizedException
     * @return 인증 실패 응답
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedException(UnauthorizedException ex) {
        log.warn("인증되지 않은 접근 시도가 감지되었습니다");

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("인증이 필요합니다."));
    }

    /**
     * 일반적인 RuntimeException 처리
     * 
     * @param ex RuntimeException
     * @return 일반 오류 응답
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        log.error("서버 오류 발생", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("서버 내부 오류가 발생했습니다."));
    }

    /**
     * 예상치 못한 모든 예외 처리
     * 
     * @param ex Exception
     * @return 예상치 못한 오류 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("예상치 못한 오류 발생", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("예상치 못한 오류가 발생했습니다."));
    }
}
