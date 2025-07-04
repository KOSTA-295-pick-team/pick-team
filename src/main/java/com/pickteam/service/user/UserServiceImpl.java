package com.pickteam.service.user;

import com.pickteam.dto.user.*;
import com.pickteam.dto.security.JwtAuthenticationResponse;
import com.pickteam.domain.user.Account;
import com.pickteam.domain.user.RefreshToken;
import com.pickteam.domain.user.UserHashtag;
import com.pickteam.domain.user.UserHashtagList;
import com.pickteam.domain.enums.UserRole;
import com.pickteam.exception.email.EmailNotVerifiedException;
import com.pickteam.exception.user.UserNotFoundException;
import com.pickteam.exception.validation.ValidationException;
import com.pickteam.exception.user.DuplicateEmailException;
import com.pickteam.exception.auth.AuthenticationException;
import com.pickteam.exception.user.AccountWithdrawalException;
import com.pickteam.constants.UserErrorMessages;
import com.pickteam.repository.user.AccountRepository;
import com.pickteam.repository.user.RefreshTokenRepository;
import com.pickteam.repository.user.UserHashtagRepository;
import com.pickteam.repository.user.UserHashtagListRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 사용자 서비스 구현체
 * - 회원가입, 로그인, 프로필 관리 등 사용자 관련 모든 비즈니스 로직 처리
 * - 이메일 인증, 비밀번호 검증, MBTI 기반 팀원 추천 기능
 * - 계정 소프트 삭제 및 보안 강화된 사용자 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final AccountRepository accountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserHashtagRepository userHashtagRepository;
    private final UserHashtagListRepository userHashtagListRepository;
    private final AuthService authService;
    private final EmailService emailService;
    private final ValidationService validationService;

    /** 기본 유예기간 (일) - 환경변수에서 주입 */
    @Value("${app.account.default-grace-period-days}")
    private int defaultGracePeriodDays;

    /**
     * 이메일 마스킹 (개인정보 보호)
     * - 로그에 이메일 출력 시 개인정보 보호를 위해 마스킹
     * 
     * @param email 원본 이메일
     * @return 마스킹된 이메일
     */
    private String maskEmail(String email) {
        if (email == null || email.length() < 3) {
            return "***@***.***";
        }

        int atIndex = email.indexOf('@');
        if (atIndex == -1) {
            return email.substring(0, Math.min(2, email.length())) + "***";
        }

        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex);

        if (localPart.length() <= 2) {
            return localPart.charAt(0) + "***" + domainPart;
        } else {
            return localPart.substring(0, 2) + "***" + domainPart;
        }
    }

    /**
     * 해시태그 목록 마스킹 (개인정보 보호)
     * - 해시태그 내용 대신 개수만 로깅
     * 
     * @param hashtags 해시태그 목록
     * @return 마스킹된 정보
     */
    private String maskHashtags(List<String> hashtags) {
        if (hashtags == null || hashtags.isEmpty()) {
            return "빈 목록";
        }
        return hashtags.size() + "개 해시태그";
    }

    /**
     * 간소화된 회원가입 처리
     * - 이메일과 패스워드만으로 기본 계정 생성
     * - 프로필 정보는 나중에 별도로 완성
     * 
     * @param request 간소화된 회원가입 요청 정보
     * @throws ValidationException       유효성 검사 실패 시
     * @throws EmailNotVerifiedException 이메일 인증 미완료 시
     * @throws DuplicateEmailException   이메일 중복 시
     */
    @Override
    public void registerUser(SignupRequest request) {
        log.info("간소화된 사용자 등록 시작: {}", maskEmail(request.getEmail()));

        // 1. 기본 유효성 검사
        if (!validationService.isValidEmail(request.getEmail())) {
            throw new ValidationException(UserErrorMessages.INVALID_EMAIL);
        }
        if (!validationService.isValidPassword(request.getPassword())) {
            throw new ValidationException(UserErrorMessages.INVALID_PASSWORD);
        }

        // 패스워드 확인 검증
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("비밀번호가 일치하지 않습니다");
        }

        // 2. 탈퇴 계정 검증 (유예 기간 중인 계정 확인)
        accountRepository.findWithdrawnAccountByEmail(request.getEmail())
                .ifPresent(withdrawnAccount -> {
                    throw new AccountWithdrawalException(
                            UserErrorMessages.ACCOUNT_WITHDRAWAL_GRACE_PERIOD,
                            withdrawnAccount.getPermanentDeletionDate());
                });

        // 3. 이메일 인증 확인
        if (!emailService.isEmailVerified(request.getEmail())) {
            throw new EmailNotVerifiedException(UserErrorMessages.EMAIL_NOT_VERIFIED);
        }

        // 4. 중복 검사 (활성 계정만 확인)
        if (accountRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
            throw new DuplicateEmailException(UserErrorMessages.DUPLICATE_EMAIL);
        }

        // 5. 간소화된 계정 생성 (이메일, 패스워드, 기본 role만)
        Account account = Account.builder()
                .email(request.getEmail())
                .password(authService.encryptPassword(request.getPassword()))
                .role(UserRole.USER) // 기본값 설정
                .build();

        accountRepository.save(account);
        log.info("간소화된 사용자 등록 완료: {}", maskEmail(request.getEmail()));
    }

    /**
     * 이메일 중복 확인
     * - 회원가입 시 이메일 중복 여부 체크 (활성 계정만)
     * 
     * @param email 확인할 이메일 주소
     * @return true: 중복됨(사용불가), false: 사용가능
     */
    @Override
    public boolean checkDuplicateId(String email) {
        // 이메일 중복 확인 (활성 계정만, true: 중복됨, false: 사용가능)
        return accountRepository.existsByEmailAndDeletedAtIsNull(email);
    }

    /**
     * 비밀번호 유효성 검사
     * - 비밀번호 복잡성 규칙 확인 (대소문자, 숫자, 특수문자, 8자리 이상)
     * 
     * @param password 검증할 비밀번호
     * @return 비밀번호 유효성 여부
     */
    @Override
    public boolean validatePassword(String password) {
        // 비밀번호 유효성 검사
        return validationService.isValidPassword(password);
    }

    /**
     * 이메일 인증 요청
     * - 이메일 형식 검증 후 6자리 인증 코드 생성
     * - 인증 코드를 DB에 저장하고 메일 발송
     * 
     * @param email 인증 메일을 받을 이메일 주소
     * @throws ValidationException 이메일 형식 오류 시
     */
    @Override
    public void requestEmailVerification(String email) {
        log.info("이메일 인증 요청: {}", maskEmail(email));

        // 1. 이메일 형식 검사
        if (!validationService.isValidEmail(email)) {
            throw new ValidationException(UserErrorMessages.INVALID_EMAIL);
        }

        // 2. 인증 코드 생성 및 발송
        String verificationCode = emailService.generateVerificationCode();
        emailService.storeVerificationCode(email, verificationCode);
        emailService.sendVerificationEmail(email, verificationCode);

        log.info("이메일 인증 코드 발송 완료: {}", maskEmail(email));
    }

    /**
     * 이메일 인증 코드 확인
     * - 사용자가 입력한 인증 코드를 DB와 비교하여 검증
     * - 만료시간 자동 확인 및 인증 성공 시 상태 업데이트
     * 
     * @param email            인증할 이메일 주소
     * @param verificationCode 사용자가 입력한 인증 코드
     * @return 인증 성공 여부
     */
    @Override
    public boolean verifyEmail(String email, String verificationCode) {
        // 이메일 인증 코드 확인
        return emailService.verifyCode(email, verificationCode);
    }

    /**
     * 사용자 로그인
     * - AuthService에 로그인 처리를 위임
     * - JWT 토큰 생성 및 사용자 정보 반환
     * 
     * @param request 로그인 요청 정보 (이메일, 비밀번호)
     * @return JWT 토큰과 사용자 정보
     */
    @Override
    public JwtAuthenticationResponse login(UserLoginRequest request) {
        // AuthService에 로그인 처리 위임
        return authService.authenticate(request);
    }

    /**
     * 내 프로필 조회
     * - 로그인된 사용자의 프로필 정보 조회
     * - 삭제되지 않은 계정만 조회 (소프트 삭제 고려)
     * 
     * @param userId 조회할 사용자 ID
     * @return 사용자 프로필 정보
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(Long userId) {
        Account account = accountRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorMessages.USER_NOT_FOUND));

        return convertToProfileResponse(account);
    }

    /**
     * 내 프로필 수정
     * - 사용자 프로필 정보 업데이트 (선택적 필드 업데이트)
     * - null이 아닌 필드만 업데이트하여 부분 수정 지원
     * - 각 필드별 유효성 검사 실시
     * 
     * @param userId  수정할 사용자 ID
     * @param request 프로필 수정 요청 정보
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     * @throws ValidationException   유효성 검사 실패 시
     */
    @Override
    public void updateMyProfile(Long userId, UserProfileUpdateRequest request) {
        log.info("프로필 수정 시작: userId={}", userId);

        Account account = accountRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorMessages.USER_NOT_FOUND));

        // 유효성 검사
        if (request.getName() != null && !validationService.isValidName(request.getName())) {
            throw new ValidationException(UserErrorMessages.INVALID_NAME);
        }
        if (request.getAge() != null && !validationService.isValidAge(request.getAge())) {
            throw new ValidationException(UserErrorMessages.INVALID_AGE_UPDATE);
        }
        if (request.getMbti() != null && !validationService.isValidMbti(request.getMbti())) {
            throw new ValidationException(UserErrorMessages.INVALID_MBTI);
        }

        // 프로필 업데이트 (null과 빈 문자열 모두 체크)
        if (request.getName() != null && !request.getName().trim().isEmpty())
            account.setName(request.getName().trim());
        if (request.getAge() != null)
            account.setAge(request.getAge());
        if (request.getMbti() != null && !request.getMbti().trim().isEmpty())
            account.setMbti(request.getMbti().trim());
        if (request.getDisposition() != null && !request.getDisposition().trim().isEmpty())
            account.setDisposition(request.getDisposition().trim());
        if (request.getIntroduction() != null && !request.getIntroduction().trim().isEmpty())
            account.setIntroduction(request.getIntroduction().trim());
        if (request.getPortfolio() != null && !request.getPortfolio().trim().isEmpty())
            account.setPortfolio(request.getPortfolio().trim());
        if (request.getProfileImageUrl() != null)
            account.setProfileImageUrl(request.getProfileImageUrl());
        if (request.getPreferWorkstyle() != null && !request.getPreferWorkstyle().trim().isEmpty())
            account.setPreferWorkstyle(request.getPreferWorkstyle().trim());
        if (request.getDislikeWorkstyle() != null && !request.getDislikeWorkstyle().trim().isEmpty())
            account.setDislikeWorkstyle(request.getDislikeWorkstyle().trim());

        // 해시태그 처리 (빈 배열도 허용 - 모든 해시태그 삭제 의미)
        if (request.getHashtags() != null) {
            updateUserHashtags(account, request.getHashtags());
        }

        accountRepository.save(account);
        log.info("프로필 수정 완료: userId={}", userId);
    }

    /**
     * 사용자 해시태그 업데이트 처리
     * - 기존 해시태그 연결을 모두 삭제하고 새로운 해시태그들로 재설정
     * 
     * @param account      사용자 계정
     * @param hashtagNames 새로운 해시태그 이름 목록
     */
    private void updateUserHashtags(Account account, List<String> hashtagNames) {
        log.debug("해시태그 업데이트 시작: userId={}, hashtags={}", account.getId(), maskHashtags(hashtagNames));

        // 1. 해시태그 전처리 및 중복 제거
        List<String> validHashtags = hashtagNames.stream()
                .filter(name -> name != null && !name.trim().isEmpty()) // null과 빈 문자열 필터링
                .map(name -> name.trim().toLowerCase()) // 정규화
                .distinct() // 중복 제거
                .filter(this::isValidHashtagName) // 유효성 검증
                .collect(Collectors.toList());

        // 2. 해시태그 개수 제한 검증 (중복 제거 후)
        if (validHashtags.size() > 20) {
            log.warn("해시태그 개수 초과: userId={}, count={}", account.getId(), validHashtags.size());
            throw new ValidationException("해시태그는 최대 20개까지 등록 가능합니다.");
        }

        // 3. 기존 해시태그 연결 모두 삭제
        userHashtagListRepository.deleteByAccount(account);

        // 4. 새로운 해시태그들 처리 (배치 작업으로 트랜잭션 안정성 향상)
        List<UserHashtagList> newHashtagLists = new ArrayList<>();
        for (String cleanedName : validHashtags) {

            // 해시태그 조회 또는 생성 (활성 해시태그만 우선 조회)
            UserHashtag userHashtag = userHashtagRepository.findByNameAndIsDeletedFalse(cleanedName)
                    .orElseGet(() -> {
                        // 삭제된 해시태그가 있는지 확인하여 복원할지, 새로 생성할지 결정
                        Optional<UserHashtag> deletedHashtag = userHashtagRepository.findByName(cleanedName);
                        if (deletedHashtag.isPresent() && deletedHashtag.get().getIsDeleted()) {
                            // 삭제된 해시태그가 있다면 복원
                            UserHashtag restored = deletedHashtag.get();
                            restored.restore();
                            log.debug("삭제된 해시태그 복원: {}", cleanedName);
                            return userHashtagRepository.save(restored);
                        } else {
                            // 새로운 해시태그 생성
                            UserHashtag newHashtag = UserHashtag.builder()
                                    .name(cleanedName)
                                    .build();
                            log.debug("새 해시태그 생성: {}", cleanedName);
                            return userHashtagRepository.save(newHashtag);
                        }
                    });

            // 사용자-해시태그 연결 생성 (리스트에 추가만 하고 저장은 나중에 배치로)
            UserHashtagList userHashtagList = UserHashtagList.builder()
                    .account(account)
                    .userHashtag(userHashtag)
                    .build();
            newHashtagLists.add(userHashtagList);
        }

        // 배치로 저장하여 성능 및 트랜잭션 안정성 향상
        userHashtagListRepository.saveAll(newHashtagLists);

        log.info("해시태그 업데이트 완료: userId={}, count={}", account.getId(), validHashtags.size());
    }

    /**
     * 해시태그 유효성 검증
     * - 길이 제한 및 허용 문자 검증
     * 
     * @param hashtagName 검증할 해시태그 이름
     * @return 유효한 해시태그면 true
     */
    private boolean isValidHashtagName(String hashtagName) {
        if (hashtagName == null || hashtagName.trim().isEmpty()) {
            return false;
        }

        // 해시태그 길이 검증 (최대 50자)
        if (hashtagName.length() > 50) {
            log.debug("해시태그 길이 초과: [MASKED]");
            return false;
        }

        // 특수문자 제거 (알파벳, 숫자, 한글만 허용)
        if (!hashtagName.matches("^[a-zA-Z0-9가-힣]*$")) {
            log.debug("유효하지 않은 해시태그 문자: [MASKED]");
            return false;
        }

        return true;
    }

    /**
     * 다른 사용자 프로필 조회
     * - 특정 사용자의 공개 프로필 정보 조회
     * - 팀원 검색 및 추천 시 사용
     * 
     * @param userId 조회할 사용자 ID
     * @return 사용자 프로필 정보
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        Account account = accountRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorMessages.USER_NOT_FOUND));

        return convertToProfileResponse(account);
    }

    /**
     * 전체 사용자 프로필 조회
     * - 등록된 모든 사용자의 프로필 목록 조회
     * - 관리자 기능 또는 전체 팀원 목록 표시에 사용
     * - 삭제된 계정은 제외하고 조회
     * 
     * @return 전체 사용자 프로필 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserProfileResponse> getAllUserProfile() {
        List<Account> accounts = accountRepository.findAllByIsDeletedFalse();
        return accounts.stream()
                .map(this::convertToProfileResponse)
                .collect(Collectors.toList());
    }

    /**
     * 추천 팀원 리스트 조회
     * - MBTI와 성향을 기반으로 현재 사용자와 호환성이 높은 팀원 추천
     * - 협업 효율성을 높이기 위한 Pick Team의 핵심 기능
     * - 본인을 제외한 추천 결과 반환
     * 
     * @param userId 추천을 요청한 사용자 ID
     * @return 추천 팀원 목록
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserProfileResponse> getRecommendedTeamMembers(Long userId) {
        Account currentUser = accountRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorMessages.USER_NOT_FOUND));

        // MBTI와 성향 기반 추천 팀원 조회
        List<Account> recommendedAccounts = accountRepository.findRecommendedTeamMembers(
                currentUser.getMbti(),
                currentUser.getDisposition(),
                userId);

        log.info("팀원 추천 조회 완료: userId={}, 추천 수={}", userId, recommendedAccounts.size());

        return recommendedAccounts.stream()
                .map(this::convertToProfileResponse)
                .collect(Collectors.toList());
    }

    /**
     * 비밀번호 변경
     * - 현재 비밀번호 확인 후 새 비밀번호로 변경
     * - 새 비밀번호 복잡성 검증 및 암호화 저장
     * - 보안을 위한 현재 비밀번호 재확인 필수
     * 
     * @param userId  비밀번호를 변경할 사용자 ID
     * @param request 비밀번호 변경 요청 정보 (현재/새 비밀번호)
     * @throws UserNotFoundException   사용자를 찾을 수 없는 경우
     * @throws AuthenticationException 현재 비밀번호 불일치 시
     * @throws ValidationException     새 비밀번호 형식 오류 시
     */
    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("비밀번호 변경 시작: userId={}", userId);

        Account account = accountRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorMessages.USER_NOT_FOUND));

        // 현재 비밀번호 확인
        if (!authService.matchesPassword(request.getCurrentPassword(), account.getPassword())) {
            log.warn("비밀번호 변경 실패 - 현재 비밀번호 불일치: userId={}", userId);
            throw new AuthenticationException(UserErrorMessages.INVALID_CURRENT_PASSWORD);
        }

        // 새 비밀번호 유효성 검사
        if (!validationService.isValidPassword(request.getNewPassword())) {
            throw new ValidationException(UserErrorMessages.INVALID_NEW_PASSWORD);
        }

        // 비밀번호 변경
        account.setPassword(authService.encryptPassword(request.getNewPassword()));
        accountRepository.save(account);
        log.info("비밀번호 변경 완료: userId={}", userId);
    }

    /**
     * 계정 삭제 (소프트 삭제)
     * - 실제 데이터 삭제가 아닌 삭제 플래그 설정
     * - 데이터 복구 가능성과 참조 무결성 보장
     * - 관련 프로젝트/팀 데이터 보존을 위한 안전한 삭제
     * 
     * @param userId 삭제할 사용자 ID
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Override
    public void deleteAccount(Long userId) {
        log.info("계정 삭제 시작: userId={}", userId);

        Account account = accountRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorMessages.USER_NOT_FOUND));

        // Soft Delete with Grace Period
        account.markDeletedWithGracePeriod(defaultGracePeriodDays);

        accountRepository.save(account);
        log.info("계정 삭제 완료 (유예기간 {}일): userId={}, permanentDeletionDate={}",
                defaultGracePeriodDays, userId, account.getPermanentDeletionDate());
    }

    /**
     * 세션 상태 확인
     * - 현재 사용자의 세션 유효성 및 로그인 정보 제공
     * - RefreshToken 존재 여부로 세션 유효성 판단
     * 
     * @param userId 사용자 ID
     * @return 세션 상태 정보
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Override
    @Transactional(readOnly = true)
    public SessionStatusResponse getSessionStatus(Long userId) {
        log.debug("세션 상태 확인 요청: userId={}", userId);

        // 사용자 조회
        Account account = accountRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorMessages.USER_NOT_FOUND));

        // RefreshToken 조회로 세션 유효성 확인
        List<RefreshToken> refreshTokens = refreshTokenRepository.findByAccount(account);
        boolean isSessionValid = !refreshTokens.isEmpty();

        // 가장 최근 RefreshToken 정보 사용
        RefreshToken latestToken = refreshTokens.isEmpty() ? null : refreshTokens.get(0);

        log.debug("세션 상태 확인 완료: userId={}, isValid={}", userId, isSessionValid);

        return SessionStatusResponse.builder()
                .isValid(isSessionValid)
                .loginTime(latestToken != null ? latestToken.getCreatedAt() : null)
                .expiresAt(latestToken != null ? latestToken.getExpiresAt() : null)
                .userId(userId)
                .email(account.getEmail())
                .build();
    }

    /**
     * 계정 정보를 프로필 응답 DTO로 변환하는 헬퍼 메서드
     * - Entity를 API 응답용 DTO로 안전하게 변환
     * - 민감한 정보(비밀번호 등) 제외하고 변환
     * - 엔티티 레벨에서 기본값이 설정되므로 null 체크 불필요
     * - 코드 중복 제거를 위한 공통 변환 로직
     * 
     * @param account 변환할 계정 엔티티
     * @return 변환된 프로필 응답 DTO
     */
    private UserProfileResponse convertToProfileResponse(Account account) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(account.getId());
        response.setEmail(account.getEmail());
        response.setName(account.getName()); // 엔티티 기본값: "신규 사용자"
        response.setAge(account.getAge()); // 나이는 숫자이므로 null 유지
        response.setRole(account.getRole());
        response.setMbti(account.getMbti()); // 엔티티 기본값: "정보없음"
        response.setDisposition(account.getDisposition()); // 엔티티 기본값: "정보없음"
        response.setIntroduction(account.getIntroduction()); // 엔티티 기본값: "정보없음"
        response.setPortfolio(account.getPortfolio()); // 엔티티 기본값: "https://github.com/myportfolio"
        // TODO: 통합 파일 시스템 구축 후 활성화
        // response.setProfileImageUrl(account.getProfileImageUrl()); // 프로필 이미지는 null
        // 유지
        // response.setProfileImageUrl(null); // 임시로 null 설정
        response.setPreferWorkstyle(account.getPreferWorkstyle()); // 엔티티 기본값: "정보없음"
        response.setDislikeWorkstyle(account.getDislikeWorkstyle()); // 엔티티 기본값: "정보없음"

        // 해시태그 목록 조회 및 설정
        List<UserHashtagList> userHashtagLists = userHashtagListRepository.findByAccountAndIsDeletedFalse(account);
        List<String> hashtags = userHashtagLists.stream()
                .map(userHashtagList -> userHashtagList.getUserHashtag().getName())
                .collect(Collectors.toList());
        response.setHashtags(hashtags);

        return response;
    }

    // === 해시태그 관리 구현 ===

    /**
     * 해시태그 검색 (자동완성용)
     * - 성능 최적화: 상위 10개 결과만 반환
     * - Soft-delete 필터링: 삭제된 해시태그 제외
     * - 입력 검증 및 정규화 처리
     */
    @Override
    @Transactional(readOnly = true)
    public List<HashtagResponse> searchHashtags(String keyword) {
        log.debug("해시태그 검색 시작: [KEYWORD_LENGTH={}]", keyword.length());

        if (keyword == null || keyword.trim().isEmpty()) {
            log.debug("빈 키워드로 인한 빈 결과 반환");
            return List.of();
        }

        String cleanedKeyword = keyword.trim().toLowerCase();

        // 성능 최적화: Top 10개만 조회, soft-delete 필터링 적용
        List<UserHashtag> hashtags = userHashtagRepository
                .findTop10ByIsDeletedFalseAndNameContainingIgnoreCaseOrderByName(cleanedKeyword);

        List<HashtagResponse> results = hashtags.stream()
                .map(HashtagResponse::from)
                .collect(Collectors.toList());

        log.debug("해시태그 검색 완료: [KEYWORD_LENGTH={}], 결과 수={} (최대 10개)", cleanedKeyword.length(), results.size());
        return results;
    }
}
