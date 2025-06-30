package com.pickteam.dto.user;

import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * 사용자 프로필 수정 요청 DTO
 * - 로그인된 사용자의 프로필 정보 업데이트 시 사용
 * - 이메일, 비밀번호 등 민감한 정보는 별도 API로 분리
 * - 팀 매칭에 영향을 주는 성향 정보 수정 포함
 */
@Data
public class UserProfileUpdateRequest {
    /** 사용자 이름 */
    @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다")
    @Pattern(regexp = "^[가-힣a-zA-Z\\s]*$", message = "이름은 한글, 영문, 공백만 입력 가능합니다")
    private String name;

    /** 사용자 나이 */
    @Min(value = 0, message = "나이는 0 이상이어야 합니다")
    @Max(value = 225, message = "나이는 225세 이하여야 합니다")
    private Integer age;

    /** MBTI 성격 유형 (팀 매칭 참고용, 선택사항) */
    @Pattern(regexp = "^(INTJ|INTP|ENTJ|ENTP|INFJ|INFP|ENFJ|ENFP|ISTJ|ISFJ|ESTJ|ESFJ|ISTP|ISFP|ESTP|ESFP)?$", message = "올바른 MBTI 형식이 아닙니다")
    private String mbti;

    /** 사용자 성향/특성 설명 (팀 매칭 참고용) */
    @Size(max = 200, message = "성향 설명은 200자 이하여야 합니다")
    private String disposition;

    /** 사용자 자기소개 */
    @Size(max = 500, message = "자기소개는 500자 이하여야 합니다")
    private String introduction;

    /** 포트폴리오 링크 또는 설명 */
    @Size(max = 200, message = "포트폴리오 링크는 200자 이하여야 합니다")
    private String portfolio;

    /** 프로필 이미지 URL (파일 업로드 후 받은 URL) */
    @Size(max = 500, message = "프로필 이미지 URL은 500자 이하여야 합니다")
    private String profileImageUrl;

    /** 선호하는 작업 스타일 (팀 매칭 알고리즘에 활용) */
    @Size(max = 100, message = "선호 작업 스타일은 100자 이하여야 합니다")
    private String preferWorkstyle;

    /** 기피하는 작업 스타일 (팀 매칭 알고리즘에서 제외) */
    @Size(max = 100, message = "기피 작업 스타일은 100자 이하여야 합니다")
    private String dislikeWorkstyle;
}
