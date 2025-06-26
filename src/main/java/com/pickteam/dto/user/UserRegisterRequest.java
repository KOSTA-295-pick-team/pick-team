package com.pickteam.dto.user;

import lombok.Data;

/**
 * 회원가입 요청 DTO
 * - 새로운 사용자 계정 생성 시 필요한 모든 정보
 */
@Data
public class UserRegisterRequest {

    /** 사용자 이메일 (로그인 ID) */
    private String email;

    /** 비밀번호 (암호화되어 저장) */
    private String password;

    /** 사용자 이름 */
    private String name;

    /** 나이 */
    private Integer age;

    /** MBTI 성격 유형 (선택사항) */
    private String mbti;

    /** 성향/특성 설명 (선택사항) */
    private String disposition;

    /** 자기소개 (선택사항) */
    private String introduction;

    /** 포트폴리오 링크 (선택사항) */
    private String portfolio;

    /** 선호하는 작업 스타일 (선택사항) */
    private String preferWorkstyle;

    /** 기피하는 작업 스타일 (선택사항) */
    private String dislikeWorkstyle;
}
