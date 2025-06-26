package com.pickteam.dto.user;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.ToString;

/**
 * 회원가입 요청 DTO
 * - 새로운 사용자 계정 생성 시 필요한 모든 정보
 */
@Data
public class UserRegisterRequest {

    /** 사용자 이메일 (로그인 ID) */
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    /** 비밀번호 (암호화되어 저장) */
    @ToString.Exclude
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 50, message = "비밀번호는 8자 이상 50자 이하여야 합니다")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).*$", message = "비밀번호는 대소문자, 숫자, 특수문자를 모두 포함해야 합니다")
    private String password;

    /** 사용자 이름 */
    @NotBlank(message = "이름은 필수입니다")
    @Size(min = 2, max = 10, message = "이름은 2자 이상 10자 이하여야 합니다")
    private String name;

    /** 나이 */
    @NotNull(message = "나이는 필수입니다")
    @Min(value = 0, message = "나이는 0 이상이어야 합니다")
    @Max(value = 150, message = "나이는 150세 이하여야 합니다")
    private Integer age;

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
