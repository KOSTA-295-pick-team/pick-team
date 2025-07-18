package com.pickteam.controller.user;

import com.pickteam.dto.user.*;
import com.pickteam.dto.ApiResponse;
import com.pickteam.domain.enums.UserRole;
import com.pickteam.domain.common.FileInfo;
import com.pickteam.service.user.UserService;
import com.pickteam.service.user.AuthService;
import com.pickteam.service.board.PostAttachService;
import com.pickteam.constants.UserControllerMessages;
import com.pickteam.exception.validation.ValidationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final PostAttachService postAttachService; // 프로필 이미지 업로드용

    // 환경변수에서 주입받는 설정들
    @Value("${app.email.blocked-domains}")
    private String blockedDomainsConfig;

    // 간소화된 회원가입
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> registerUser(@Valid @RequestBody SignupRequest request) {
        log.info("간소화된 회원가입 요청 - 이메일: {}", maskEmail(request.getEmail()));

        // 추가 검증: 이메일 도메인 블랙리스트 체크 (보안 강화)
        if (request.getEmail() != null && isBlockedEmailDomain(request.getEmail())) {
            log.warn("차단된 이메일 도메인 회원가입 시도: {}", maskEmail(request.getEmail()));
            throw new ValidationException("지원하지 않는 이메일 도메인입니다.");
        }

        userService.registerUser(request);
        log.info("간소화된 회원가입 완료 - 이메일: {}", maskEmail(request.getEmail()));
        return ResponseEntity.ok(ApiResponse.success(UserControllerMessages.REGISTER_SUCCESS, null));
    }

    // ID 중복검사
    @PostMapping("/check-id")
    public ResponseEntity<ApiResponse<Boolean>> checkDuplicateId(@Valid @RequestBody CheckDuplicateIdRequest request) {
        log.debug("이메일 중복 검사 요청 - 이메일: {}", maskEmail(request.getEmail()));
        boolean isDuplicate = userService.checkDuplicateId(request.getEmail());
        log.debug("이메일 중복 검사 결과 - 이메일: {}, 중복여부: {}", maskEmail(request.getEmail()), isDuplicate);
        return ResponseEntity.ok(ApiResponse.success(UserControllerMessages.CHECK_DUPLICATE_SUCCESS, !isDuplicate));
    }

    // 비밀번호 유효성 검사
    @PostMapping("/validate-password")
    public ResponseEntity<ApiResponse<Boolean>> validatePassword(@Valid @RequestBody ValidatePasswordRequest request) {
        log.debug("비밀번호 유효성 검사 요청");
        boolean isValid = userService.validatePassword(request.getPassword());
        log.debug("비밀번호 유효성 검사 결과: {}", isValid);
        return ResponseEntity.ok(ApiResponse.success(UserControllerMessages.PASSWORD_VALIDATION_SUCCESS, isValid));
    }

    // 세션 상태 확인
    @GetMapping("/session/status")
    public ResponseEntity<ApiResponse<SessionStatusResponse>> getSessionStatus() {
        log.debug("세션 상태 확인 요청");
        // 인증 확인 및 사용자 ID 추출
        Long currentUserId = authService.requireAuthentication();

        log.debug("세션 상태 확인 - 사용자 ID: {}", currentUserId);
        SessionStatusResponse sessionStatus = userService.getSessionStatus(currentUserId);
        log.debug("세션 상태 확인 완료 - 사용자 ID: {}, 세션 유효: {}", currentUserId, sessionStatus.isValid());

        return ResponseEntity.ok(ApiResponse.success("세션 상태 조회 성공", sessionStatus));
    }

    // 내 프로필 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile() {
        log.debug("내 프로필 조회 요청");
        // 인증 확인 및 사용자 ID 추출
        Long currentUserId = authService.requireAuthentication();

        log.debug("내 프로필 조회 - 사용자 ID: {}", currentUserId);
        UserProfileResponse profile = userService.getMyProfile(currentUserId);
        log.info("내 프로필 조회 완료 - 사용자 ID: {}", currentUserId);
        return ResponseEntity.ok(ApiResponse.success(UserControllerMessages.PROFILE_GET_SUCCESS, profile));
    }

    // 내 프로필 수정 (신규 프로필 작성 포함)
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<Void>> updateMyProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        log.debug("내 프로필 수정 요청");
        // 인증 확인 및 사용자 ID 추출
        Long currentUserId = authService.requireAuthentication();

        log.info("내 프로필 수정 시작 - 사용자 ID: {}", currentUserId);
        userService.updateMyProfile(currentUserId, request);
        log.info("내 프로필 수정 완료 - 사용자 ID: {}", currentUserId);
        return ResponseEntity.ok(ApiResponse.success(UserControllerMessages.PROFILE_UPDATE_SUCCESS, null));
    }

    // 다른 사람의 프로필 조회
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(
            @PathVariable @Positive(message = "사용자 ID는 양수여야 합니다.") Long userId) {
        log.debug("다른 사용자 프로필 조회 요청 - 대상 사용자 ID: {}", userId);

        // 추가 검증: 사용자 ID 범위 체크 (보안 강화)
        if (userId > Long.MAX_VALUE / 2) {
            log.warn("비정상적인 사용자 ID 접근 시도: {}", userId);
            throw new ValidationException("유효하지 않은 사용자 ID입니다.");
        }

        UserProfileResponse profile = userService.getUserProfile(userId);
        log.debug("다른 사용자 프로필 조회 완료 - 대상 사용자 ID: {}", userId);
        return ResponseEntity.ok(ApiResponse.success(UserControllerMessages.USER_PROFILE_GET_SUCCESS, profile));
    }

    // 전체 사용자 프로필 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> getAllUserProfile() {
        log.debug("전체 사용자 프로필 조회 요청");
        List<UserProfileResponse> profiles = userService.getAllUserProfile();
        log.info("전체 사용자 프로필 조회 완료 - 조회된 사용자 수: {}", profiles.size());
        return ResponseEntity.ok(ApiResponse.success(UserControllerMessages.ALL_USER_PROFILE_GET_SUCCESS, profiles));
    }

    // 추천 팀원 리스트 조회
    @GetMapping("/recommend")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> getRecommendedTeamMembers() {
        log.debug("추천 팀원 조회 요청");
        // 인증 확인 및 사용자 ID 추출
        Long currentUserId = authService.requireAuthentication();

        log.debug("추천 팀원 조회 - 사용자 ID: {}", currentUserId);
        List<UserProfileResponse> recommendedMembers = userService.getRecommendedTeamMembers(currentUserId);
        log.info("추천 팀원 조회 완료 - 사용자 ID: {}, 추천된 팀원 수: {}", currentUserId, recommendedMembers.size());
        return ResponseEntity
                .ok(ApiResponse.success(UserControllerMessages.RECOMMENDED_MEMBERS_GET_SUCCESS, recommendedMembers));
    }

    // 비밀번호 변경
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.debug("비밀번호 변경 요청");
        // 인증 확인 및 사용자 ID 추출
        Long currentUserId = authService.requireAuthentication();

        // 추가 검증: 새 비밀번호와 기존 비밀번호가 동일한지 체크
        if (request.getCurrentPassword() != null && request.getNewPassword() != null &&
                request.getCurrentPassword().equals(request.getNewPassword())) {
            log.warn("동일한 비밀번호로 변경 시도 - 사용자 ID: {}", currentUserId);
            throw new ValidationException("새 비밀번호는 기존 비밀번호와 달라야 합니다.");
        }

        log.info("비밀번호 변경 시작 - 사용자 ID: {}", currentUserId);
        userService.changePassword(currentUserId, request);
        log.info("비밀번호 변경 완료 - 사용자 ID: {}", currentUserId);
        return ResponseEntity.ok(ApiResponse.success(UserControllerMessages.PASSWORD_CHANGE_SUCCESS, null));
    }

    // 계정 삭제
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteAccount() {
        log.debug("계정 삭제 요청");
        // 인증 확인 및 사용자 ID 추출
        Long currentUserId = authService.requireAuthentication();

        // 추가 검증: 관리자 계정 보호
        UserProfileResponse userProfile = userService.getMyProfile(currentUserId);
        if (userProfile.getRole() == UserRole.ADMIN) {
            log.error("관리자 계정 삭제 시도 - 사용자 ID: {}", currentUserId);
            throw new ValidationException("관리자 계정은 삭제할 수 없습니다.");
        }

        log.warn("계정 삭제 시작 - 사용자 ID: {} (중요: 계정 삭제 작업)", currentUserId);
        userService.deleteAccount(currentUserId);
        log.warn("계정 삭제 완료 - 사용자 ID: {} (중요: 계정 삭제 완료)", currentUserId);
        return ResponseEntity.ok(ApiResponse.success(UserControllerMessages.ACCOUNT_DELETE_SUCCESS, null));
    }

    /**
     * 차단된 이메일 도메인 체크
     * - 임시 이메일 서비스나 알려진 스팸 도메인 차단
     * - 보안 강화를 위한 추가 검증
     * 
     * @param email 검증할 이메일 주소
     * @return 차단된 도메인이면 true, 그렇지 않으면 false
     */
    private boolean isBlockedEmailDomain(String email) {
        if (email == null || !email.contains("@")) {
            return false;
        }

        String domain = email.substring(email.lastIndexOf("@") + 1).toLowerCase();

        // 환경변수에서 차단 도메인 목록 읽어오기
        String[] blockedDomains = blockedDomainsConfig.split(",");

        for (String blockedDomain : blockedDomains) {
            if (domain.equals(blockedDomain.trim())) {
                return true;
            }
        }

        return false;
    }

    // ==================== 해시태그 관리 API ====================

    // 해시태그 검색 (자동완성용)
    @GetMapping("/hashtags/search")
    public ResponseEntity<ApiResponse<List<HashtagResponse>>> searchHashtags(
            @RequestParam String keyword) {
        log.debug("해시태그 검색 요청 - 키워드 길이: {}", keyword != null ? keyword.length() : 0);

        // 추가 검증: 키워드 길이 및 형식 체크
        if (keyword == null || keyword.trim().isEmpty()) {
            log.warn("빈 키워드로 해시태그 검색 시도");
            throw new ValidationException("검색 키워드는 필수입니다.");
        }
        if (keyword.length() > 50) {
            log.warn("너무 긴 키워드로 해시태그 검색 시도: 길이={}", keyword.length());
            throw new ValidationException("검색 키워드는 50자 이하여야 합니다.");
        }

        List<HashtagResponse> hashtags = userService.searchHashtags(keyword.trim());
        log.info("해시태그 검색 완료 - 키워드 길이: {}, 결과 수: {}", keyword.length(), hashtags.size());
        return ResponseEntity.ok(ApiResponse.success("해시태그 검색 성공", hashtags));
    }

    // ==================== 프로필 이미지 관리 API ====================

    // 프로필 이미지 업로드
    @PostMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<String>> uploadProfileImage(
            @RequestParam("file") MultipartFile file) {
        log.debug("프로필 이미지 업로드 요청 - 파일크기: {}", file.getSize());
        // 인증 확인 및 사용자 ID 추출
        Long currentUserId = authService.requireAuthentication();

        // 추가 검증: 파일 존재 여부 확인
        if (file.isEmpty()) {
            log.warn("빈 파일 업로드 시도 - 사용자 ID: {}", currentUserId);
            throw new ValidationException("업로드할 파일을 선택해주세요.");
        }

        log.info("프로필 이미지 업로드 시작 - 사용자 ID: {}, 파일크기: {}", currentUserId, file.getSize());

        // 기존 프로필 이미지 확인
        UserProfileResponse currentProfile = userService.getMyProfile(currentUserId);
        String oldImageUrl = currentProfile.getProfileImageUrl();

        // 새 프로필 이미지 업로드 및 기존 이미지 교체 (트랜잭션 안전성 보장)
        FileInfo fileInfo = postAttachService.uploadProfileImageWithReplace(file, currentUserId, oldImageUrl);
        String imageUrl = postAttachService.generateProfileImageUrl(fileInfo.getNameHashed());

        // DB에 프로필 이미지 URL 저장
        UserProfileUpdateRequest updateRequest = new UserProfileUpdateRequest();
        updateRequest.setProfileImageUrl(imageUrl);
        userService.updateMyProfile(currentUserId, updateRequest);

        log.info("프로필 이미지 업로드 완료 - 사용자 ID: {}, URL: {}", currentUserId, imageUrl);
        return ResponseEntity.ok(ApiResponse.success("프로필 이미지 업로드 성공", imageUrl));
    }

    // 프로필 이미지 삭제
    @DeleteMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<Void>> deleteProfileImage() {
        log.debug("프로필 이미지 삭제 요청");
        // 인증 확인 및 사용자 ID 추출
        Long currentUserId = authService.requireAuthentication();

        // 현재 프로필 정보 조회
        UserProfileResponse profile = userService.getMyProfile(currentUserId);
        if (profile.getProfileImageUrl() == null || profile.getProfileImageUrl().trim().isEmpty()) {
            log.warn("삭제할 프로필 이미지가 없음 - 사용자 ID: {}", currentUserId);
            throw new ValidationException("삭제할 프로필 이미지가 없습니다.");
        }

        log.info("프로필 이미지 삭제 시작 - 사용자 ID: {}", currentUserId);

        // URL에서 파일명 추출 (예: "/profile-images/uuid-filename.jpg" -> "uuid-filename.jpg")
        String imageUrl = profile.getProfileImageUrl();
        String hashedFileName = postAttachService.extractFileNameFromUrl(imageUrl);

        // 실제 파일 삭제 (PostAttachService 활용)
        postAttachService.deleteProfileImageByFileName(hashedFileName, currentUserId);

        // DB에서 프로필 이미지 URL 제거
        UserProfileUpdateRequest updateRequest = new UserProfileUpdateRequest();
        updateRequest.setProfileImageUrl(null);
        userService.updateMyProfile(currentUserId, updateRequest);

        log.info("프로필 이미지 삭제 완료 - 사용자 ID: {}", currentUserId);
        return ResponseEntity.ok(ApiResponse.success("프로필 이미지 삭제 성공", null));
    }

    /**
     * 이메일 마스킹 처리 (보안)
     */
    private String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "[EMPTY]";
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return "[INVALID_EMAIL]";
        }

        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        if (localPart.length() <= 2) {
            return "**" + domain;
        } else {
            return localPart.substring(0, 2) + "***" + domain;
        }
    }

}
