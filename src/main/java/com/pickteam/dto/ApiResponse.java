package com.pickteam.dto;

import com.pickteam.constants.SessionErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private String errorCode; // 에러 코드 추가
    private T data;

    /**
     * 성공 응답 생성 (데이터 포함)
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "성공", null, data);
    }

    /**
     * 성공 응답 생성 (커스텀 메시지와 데이터 포함)
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, null, data);
    }

    /**
     * 에러 응답 생성 (메시지만)
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, null);
    }

    /**
     * 에러 응답 생성 (SessionErrorCode 사용)
     */
    public static <T> ApiResponse<T> error(SessionErrorCode errorCode) {
        return new ApiResponse<>(false, errorCode.getMessage(), errorCode.getCode(), null);
    }

    /**
     * 에러 응답 생성 (SessionErrorCode와 커스텀 메시지)
     */
    public static <T> ApiResponse<T> error(SessionErrorCode errorCode, String customMessage) {
        return new ApiResponse<>(false, customMessage, errorCode.getCode(), null);
    }
}
