package com.pickteam.service.user;

import com.pickteam.dto.user.*;
import com.pickteam.dto.security.JwtAuthenticationResponse;

public interface UserService {
    // 회원가입
    void registerUser(UserRegisterRequest request);

    // ID 중복검사
    boolean checkDuplicateId(String email);

    // 비밀번호 유효성 검사
    boolean validatePassword(String password);

    // 메일 인증 요청
    void requestEmailVerification(String email);

    // 메일 인증 확인
    boolean verifyEmail(String email, String verificationCode);

    // 로그인
    JwtAuthenticationResponse login(UserLoginRequest request);

    // 내 프로필 조회
    UserProfileResponse getMyProfile(Long userId);

    // 내 프로필 수정
    void updateMyProfile(Long userId, UserProfileUpdateRequest request);

    // 다른 사람의 프로필 조회
    UserProfileResponse getUserProfile(Long userId);

    // 전체 사용자 프로필 조회
    java.util.List<UserProfileResponse> getAllUserProfile();

    // 추천 팀원 리스트 조회
    java.util.List<UserProfileResponse> getRecommendedTeamMembers(Long userId);

    // 비밀번호 변경
    void changePassword(Long userId, ChangePasswordRequest request);

    // 계정 삭제
    void deleteAccount(Long userId);
}
