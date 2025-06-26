package com.pickteam.controller.user;

import com.pickteam.dto.user.*;
import com.pickteam.dto.security.JwtAuthenticationResponse;
import com.pickteam.dto.ApiResponse;
import com.pickteam.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> registerUser(@Valid @RequestBody UserRegisterRequest request) {
        userService.registerUser(request);
        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다.", null));
    }

    // ID 중복검사
    @PostMapping("/check-id")
    public ResponseEntity<ApiResponse<Boolean>> checkDuplicateId(@Valid @RequestBody CheckDuplicateIdRequest request) {
        boolean isDuplicate = userService.checkDuplicateId(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(!isDuplicate));
    }

    // 비밀번호 유효성 검사
    @PostMapping("/validate-password")
    public ResponseEntity<ApiResponse<Boolean>> validatePassword(@Valid @RequestBody ValidatePasswordRequest request) {
        boolean isValid = userService.validatePassword(request.getPassword());
        return ResponseEntity.ok(ApiResponse.success(isValid));
    }

    // 메일 인증 요청
    @PostMapping("/email/request")
    public ResponseEntity<ApiResponse<Void>> requestEmailVerification(
            @Valid @RequestBody EmailVerificationRequest request) {
        userService.requestEmailVerification(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("인증 메일이 발송되었습니다.", null));
    }

    // 메일 인증 확인
    @PostMapping("/email/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyEmail(
            @Valid @RequestBody EmailVerificationConfirmRequest request) {
        boolean isVerified = userService.verifyEmail(request.getEmail(), request.getVerificationCode());
        return ResponseEntity.ok(ApiResponse.success(isVerified));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> login(@Valid @RequestBody UserLoginRequest request) {
        JwtAuthenticationResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("로그인 성공", response));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // TODO: 세션/토큰 무효화 로직 구현
        return ResponseEntity.ok(ApiResponse.success("로그아웃되었습니다.", null));
    }

    // 내 프로필 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile() {
        // TODO: 현재 로그인된 사용자 ID 가져오기 (세션/JWT에서)
        Long currentUserId = 1L; // 임시
        UserProfileResponse profile = userService.getMyProfile(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    // 내 프로필 수정
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<Void>> updateMyProfile(@RequestBody UserProfileUpdateRequest request) {
        // TODO: 현재 로그인된 사용자 ID 가져오기
        Long currentUserId = 1L; // 임시
        userService.updateMyProfile(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success("프로필이 수정되었습니다.", null));
    }

    // 다른 사람의 프로필 조회
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(@PathVariable Long userId) {
        UserProfileResponse profile = userService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    // 전체 사용자 프로필 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> getAllUserProfile() {
        List<UserProfileResponse> profiles = userService.getAllUserProfile();
        return ResponseEntity.ok(ApiResponse.success(profiles));
    }

    // 추천 팀원 리스트 조회
    @GetMapping("/recommend")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> getRecommendedTeamMembers() {
        // TODO: 현재 로그인된 사용자 ID 가져오기
        Long currentUserId = 1L; // 임시
        List<UserProfileResponse> recommendedMembers = userService.getRecommendedTeamMembers(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(recommendedMembers));
    }

    // 비밀번호 변경
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@RequestBody ChangePasswordRequest request) {
        // TODO: 현재 로그인된 사용자 ID 가져오기
        Long currentUserId = 1L; // 임시
        userService.changePassword(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success("비밀번호가 변경되었습니다.", null));
    }

    // 계정 삭제
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteAccount() {
        // TODO: 현재 로그인된 사용자 ID 가져오기
        Long currentUserId = 1L; // 임시
        userService.deleteAccount(currentUserId);
        return ResponseEntity.ok(ApiResponse.success("계정이 삭제되었습니다.", null));
    }
}
