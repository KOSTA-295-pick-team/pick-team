package com.pickteam.exception.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * RFC 9457 Problem Details for HTTP APIs 표준 응답 클래스
 * - 표준화된 에러 응답 구조 제공
 * - HTTP API 에러에 대한 구조적 정보 포함
 * - 확장 가능한 추가 필드 지원
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProblemDetail {

    /**
     * 문제 유형을 식별하는 URI 또는 문자열
     * 예: "USER_WITHDRAWAL_IN_PROGRESS"
     */
    private final String type;

    /**
     * 문제에 대한 간단한 영문 제목
     * 예: "Account Withdrawal In Progress"
     */
    private final String title;

    /**
     * HTTP 상태 코드
     * 예: 409
     */
    private final Integer status;

    /**
     * 문제에 대한 상세 설명 (한국어)
     * 예: "해당 이메일은 탈퇴 처리 중입니다. 완전 삭제까지 대기 기간이 남아있습니다."
     */
    private final String detail;

    /**
     * 문제가 발생한 특정 인스턴스의 URI
     * 예: "/api/users/email/request"
     */
    private final String instance;

    /**
     * 확장 필드들 - 에러별 추가 정보
     * 예: permanentDeletionDate, remainingDays, email 등
     */
    private final Map<String, Object> extensions;

    /**
     * 에러 발생 시각
     */
    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 빠른 생성을 위한 정적 팩토리 메서드들
     */
    public static ProblemDetail of(String type, String title, Integer status, String detail, String instance) {
        return ProblemDetail.builder()
                .type(type)
                .title(title)
                .status(status)
                .detail(detail)
                .instance(instance)
                .build();
    }

    public static ProblemDetail of(String type, String title, Integer status, String detail, String instance,
            Map<String, Object> extensions) {
        return ProblemDetail.builder()
                .type(type)
                .title(title)
                .status(status)
                .detail(detail)
                .instance(instance)
                .extensions(extensions)
                .build();
    }

    /**
     * 확장 필드 값 가져오기
     */
    public Object getExtension(String key) {
        return extensions != null ? extensions.get(key) : null;
    }

    /**
     * 확장 필드 값 가져오기 (타입 안전)
     */
    @SuppressWarnings("unchecked")
    public <T> T getExtension(String key, Class<T> type) {
        Object value = getExtension(key);
        return type.isInstance(value) ? (T) value : null;
    }
}
