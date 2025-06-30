package com.pickteam.service.user;

import com.pickteam.dto.user.*;
import com.pickteam.dto.security.JwtAuthenticationResponse;
import com.pickteam.domain.user.Account;
import com.pickteam.domain.user.RefreshToken;
import com.pickteam.domain.enums.UserRole;
import com.pickteam.exception.EmailNotVerifiedException;
import com.pickteam.exception.UserNotFoundException;
import com.pickteam.exception.ValidationException;
import com.pickteam.exception.DuplicateEmailException;
import com.pickteam.exception.AuthenticationException;
import com.pickteam.constants.UserErrorMessages;
import com.pickteam.repository.user.AccountRepository;
import com.pickteam.repository.user.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
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
    private final AuthService authService;
    private final EmailService emailService;
    private final ValidationService validationService;

    /**
     * 회원가입 처리
     * - 입력값 유효성 검사 (이메일, 비밀번호, 이름, 나이, MBTI)
     * - 이메일 중복 확인 후 계정 생성
     * - 비밀번호 암호화 및 기본 역할(USER) 설정
     * 
     * @param request 회원가입 요청 정보
     * @throws ValidationException       유효성 검사 실패 시
     * @throws EmailNotVerifiedException 이메일 인증 미완료 시
     * @throws DuplicateEmailException   이메일 중복 시
     */
    @Override
    public void registerUser(UserRegisterRequest request) {
        log.info("사용자 등록 시작: {}", request.getEmail());

        // 1. 유효성 검사
        if (!validationService.isValidEmail(request.getEmail())) {
            throw new ValidationException(UserErrorMessages.INVALID_EMAIL);
        }
        if (!validationService.isValidPassword(request.getPassword())) {
            throw new ValidationException(UserErrorMessages.INVALID_PASSWORD);
        }
        if (!validationService.isValidName(request.getName())) {
            throw new ValidationException(UserErrorMessages.INVALID_NAME);
        }
        if (!validationService.isValidAge(request.getAge())) {
            throw new ValidationException(UserErrorMessages.INVALID_AGE_REGISTER);
        }
        if (request.getMbti() != null && !validationService.isValidMbti(request.getMbti())) {
            throw new ValidationException(UserErrorMessages.INVALID_MBTI);
        }

        // 2. 이메일 인증 확인
        if (!emailService.isEmailVerified(request.getEmail())) {
            throw new EmailNotVerifiedException(UserErrorMessages.EMAIL_NOT_VERIFIED);
        }

        // 3. 중복 검사 (활성 계정만 확인)
        if (accountRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
            throw new DuplicateEmailException(UserErrorMessages.DUPLICATE_EMAIL);
        }

        // 4. 계정 생성
        Account account = Account.builder()
                .email(request.getEmail())
                .password(authService.encryptPassword(request.getPassword()))
                .name(request.getName())
                .age(request.getAge())
                .role(UserRole.USER)
                .mbti(request.getMbti())
                .disposition(request.getDisposition())
                .introduction(request.getIntroduction())
                .portfolio(request.getPortfolio())
                .preferWorkstyle(request.getPreferWorkstyle())
                .dislikeWorkstyle(request.getDislikeWorkstyle())
                .build();

        accountRepository.save(account);
        log.info("사용자 등록 완료: {}", request.getEmail());
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
        log.info("이메일 인증 요청: {}", email);

        // 1. 이메일 형식 검사
        if (!validationService.isValidEmail(email)) {
            throw new ValidationException(UserErrorMessages.INVALID_EMAIL);
        }

        // 2. 인증 코드 생성 및 발송
        String verificationCode = emailService.generateVerificationCode();
        emailService.storeVerificationCode(email, verificationCode);
        emailService.sendVerificationEmail(email, verificationCode);

        log.info("이메일 인증 코드 발송 완료: {}", email);
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
        Account account = accountRepository.findByIdAndDeletedAtIsNull(userId)
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

        Account account = accountRepository.findByIdAndDeletedAtIsNull(userId)
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

        // 프로필 업데이트
        if (request.getName() != null)
            account.setName(request.getName());
        if (request.getAge() != null)
            account.setAge(request.getAge());
        if (request.getMbti() != null)
            account.setMbti(request.getMbti());
        if (request.getDisposition() != null)
            account.setDisposition(request.getDisposition());
        if (request.getIntroduction() != null)
            account.setIntroduction(request.getIntroduction());
        if (request.getPortfolio() != null)
            account.setPortfolio(request.getPortfolio());
        if (request.getPreferWorkstyle() != null)
            account.setPreferWorkstyle(request.getPreferWorkstyle());
        if (request.getDislikeWorkstyle() != null)
            account.setDislikeWorkstyle(request.getDislikeWorkstyle());

        accountRepository.save(account);
        log.info("프로필 수정 완료: userId={}", userId);
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
        Account account = accountRepository.findByIdAndDeletedAtIsNull(userId)
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
        List<Account> accounts = accountRepository.findAllByDeletedAtIsNull();
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
        Account currentUser = accountRepository.findByIdAndDeletedAtIsNull(userId)
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

        Account account = accountRepository.findByIdAndDeletedAtIsNull(userId)
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

        Account account = accountRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorMessages.USER_NOT_FOUND));

        // Soft Delete 실행 (유예기간 설정)
        account.markDeletedWithDefaultGracePeriod();
        accountRepository.save(account);
        log.info("계정 삭제 완료 (유예기간 {}일): userId={}, permanentDeletionDate={}",
                30, userId, account.getPermanentDeletionDate());
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
        Account account = accountRepository.findByIdAndDeletedAtIsNull(userId)
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
     * - 코드 중복 제거를 위한 공통 변환 로직
     * 
     * @param account 변환할 계정 엔티티
     * @return 변환된 프로필 응답 DTO
     */
    private UserProfileResponse convertToProfileResponse(Account account) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(account.getId());
        response.setEmail(account.getEmail());
        response.setName(account.getName());
        response.setAge(account.getAge());
        response.setRole(account.getRole());
        response.setMbti(account.getMbti());
        response.setDisposition(account.getDisposition());
        response.setIntroduction(account.getIntroduction());
        response.setPortfolio(account.getPortfolio());
        response.setPreferWorkstyle(account.getPreferWorkstyle());
        response.setDislikeWorkstyle(account.getDislikeWorkstyle());
        return response;
    }
}
