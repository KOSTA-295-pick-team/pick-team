package com.pickteam.dto.user;

import com.pickteam.domain.enums.UserRole;
import lombok.Data;

/**
 * 사용자 프로필 응답 DTO
 * - 사용자 정보 조회 시 반환되는 프로필 데이터
 * - 민감한 정보(비밀번호 등) 제외하고 공개 가능한 정보만 포함
 * - 팀 매칭을 위한 성향 및 작업 스타일 정보 포함
 */
@Data
public class UserProfileResponse {
    /** 사용자 고유 식별자 */
    private Long id;

    /** 사용자 이메일 주소 */
    private String email;

    /** 사용자 이름 */
    private String name;

    /** 사용자 나이 */
    private Integer age;

    /** 사용자 권한 (ADMIN, USER 등) */
    private UserRole role;

    /** MBTI 성격 유형 (팀 매칭 참고용) */
    private String mbti;

    /** 사용자 성향/특성 설명 */
    private String disposition;

    /** 사용자 자기소개 */
    private String introduction;

    /** 포트폴리오 링크 또는 설명 */
    private String portfolio;

    /** 선호하는 작업 스타일 (팀 매칭 알고리즘 활용) */
    private String preferWorkstyle;

    /** 기피하는 작업 스타일 (팀 매칭 시 제외) */
    private String dislikeWorkstyle;
}
