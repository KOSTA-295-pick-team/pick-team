package com.pickteam.dto.user;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 프로필 완성 요청 DTO
 * - 로그인 후 사용자 프로필 정보 입력/수정 시 사용
 * - 모든 필드는 선택적 (기본 회원가입 후 점진적 완성)
 * - 이메일/패스워드는 이미 인증된 상태이므로 제외
 */
@Data
public class ProfileCompletionRequest {

    /** 사용자 이름 (선택적) */
    @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다")
    private String name;

    /** 나이 (선택적) */
    @Min(value = 0, message = "나이는 0 이상이어야 합니다")
    @Max(value = 225, message = "나이는 225세 이하여야 합니다")
    private Integer age;

    /** 사용자 역할 (선택적) */
    private String role;

    /** MBTI 성격 유형 (선택사항) */
    @Pattern(regexp = "^(INTJ|INTP|ENTJ|ENTP|INFJ|INFP|ENFJ|ENFP|ISTJ|ISFJ|ESTJ|ESFJ|ISTP|ISFP|ESTP|ESFP)?$", message = "올바른 MBTI 형식이 아닙니다")
    private String mbti;

    /** 성향/특성 설명 (선택사항) */
    @Size(max = 200, message = "성향 설명은 200자 이하여야 합니다")
    private String disposition;

    /** 자기소개 (선택사항) */
    @Size(max = 500, message = "자기소개는 500자 이하여야 합니다")
    private String introduction;

    /** 포트폴리오 링크 (선택사항) */
    @Size(max = 200, message = "포트폴리오 링크는 200자 이하여야 합니다")
    private String portfolio;

    /** 선호하는 작업 스타일 (선택사항) */
    @Size(max = 200, message = "선호하는 작업 스타일은 200자 이하여야 합니다")
    private String preferWorkstyle;

    /** 기피하는 작업 스타일 (선택사항) */
    @Size(max = 200, message = "기피하는 작업 스타일은 200자 이하여야 합니다")
    private String dislikeWorkstyle;
}
