package com.pickteam.dto.user;

import lombok.Data;

/**
 * 사용자 프로필 수정 요청 DTO
 * - 로그인된 사용자의 프로필 정보 업데이트 시 사용
 * - 이메일, 비밀번호 등 민감한 정보는 별도 API로 분리
 * - 팀 매칭에 영향을 주는 성향 정보 수정 포함
 */
@Data
public class UserProfileUpdateRequest {
    /** 사용자 이름 */
    private String name;

    /** 사용자 나이 */
    private Integer age;

    /** MBTI 성격 유형 (팀 매칭 참고용, 선택사항) */
    private String mbti;

    /** 사용자 성향/특성 설명 (팀 매칭 참고용) */
    private String disposition;

    /** 사용자 자기소개 */
    private String introduction;

    /** 포트폴리오 링크 또는 설명 */
    private String portfolio;

    /** 선호하는 작업 스타일 (팀 매칭 알고리즘에 활용) */
    private String preferWorkstyle;

    /** 기피하는 작업 스타일 (팀 매칭 알고리즘에서 제외) */
    private String dislikeWorkstyle;
}
