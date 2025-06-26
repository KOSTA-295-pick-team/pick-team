package com.pickteam.service.user;

import com.pickteam.dto.user.*;
import com.pickteam.dto.security.JwtAuthenticationResponse;
import com.pickteam.domain.user.Account;
import com.pickteam.domain.enums.UserRole;
import com.pickteam.repository.user.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final AccountRepository accountRepository;
    private final AuthService authService;
    private final EmailService emailService;
    private final ValidationService validationService;

    @Override
    public void registerUser(UserRegisterRequest request) {
        // 1. 유효성 검사
        if (!validationService.isValidEmail(request.getEmail())) {
            throw new RuntimeException("이메일 형식이 올바르지 않습니다.");
        }
        if (!validationService.isValidPassword(request.getPassword())) {
            throw new RuntimeException("비밀번호 형식이 올바르지 않습니다.");
        }
        if (!validationService.isValidName(request.getName())) {
            throw new RuntimeException("이름 형식이 올바르지 않습니다.");
        }
        if (!validationService.isValidAge(request.getAge())) {
            throw new RuntimeException("나이는 14세 이상 100세 이하여야 합니다.");
        }
        if (request.getMbti() != null && !validationService.isValidMbti(request.getMbti())) {
            throw new RuntimeException("MBTI 형식이 올바르지 않습니다.");
        }

        // 2. 중복 검사
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }

        // 3. 계정 생성
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
    }

    @Override
    public boolean checkDuplicateId(String email) {
        // 이메일 중복 확인 (true: 중복됨, false: 사용가능)
        return accountRepository.existsByEmail(email);
    }

    @Override
    public boolean validatePassword(String password) {
        // 비밀번호 유효성 검사
        return validationService.isValidPassword(password);
    }

    @Override
    public void requestEmailVerification(String email) {
        // 1. 이메일 형식 검사
        if (!validationService.isValidEmail(email)) {
            throw new RuntimeException("이메일 형식이 올바르지 않습니다.");
        }

        // 2. 인증 코드 생성 및 발송
        String verificationCode = emailService.generateVerificationCode();
        emailService.storeVerificationCode(email, verificationCode);
        emailService.sendVerificationEmail(email, verificationCode);
    }

    @Override
    public boolean verifyEmail(String email, String verificationCode) {
        // 이메일 인증 코드 확인
        return emailService.verifyCode(email, verificationCode);
    }

    @Override
    public JwtAuthenticationResponse login(UserLoginRequest request) {
        // AuthService에 로그인 처리 위임
        return authService.authenticate(request);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(Long userId) {
        Account account = accountRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return convertToProfileResponse(account);
    }

    @Override
    public void updateMyProfile(Long userId, UserProfileUpdateRequest request) {
        Account account = accountRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 유효성 검사
        if (request.getName() != null && !validationService.isValidName(request.getName())) {
            throw new RuntimeException("이름 형식이 올바르지 않습니다.");
        }
        if (request.getAge() != null && !validationService.isValidAge(request.getAge())) {
            throw new RuntimeException("나이는 14세 이상 100세 이하여야 합니다.");
        }
        if (request.getMbti() != null && !validationService.isValidMbti(request.getMbti())) {
            throw new RuntimeException("MBTI 형식이 올바르지 않습니다.");
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
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        Account account = accountRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return convertToProfileResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileResponse> getAllUserProfile() {
        List<Account> accounts = accountRepository.findAllByDeletedAtIsNull();
        return accounts.stream()
                .map(this::convertToProfileResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileResponse> getRecommendedTeamMembers(Long userId) {
        Account currentUser = accountRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // MBTI와 성향 기반 추천 팀원 조회
        List<Account> recommendedAccounts = accountRepository.findRecommendedTeamMembers(
                currentUser.getMbti(),
                currentUser.getDisposition(),
                userId);

        return recommendedAccounts.stream()
                .map(this::convertToProfileResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        Account account = accountRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 현재 비밀번호 확인
        if (!authService.matchesPassword(request.getCurrentPassword(), account.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 올바르지 않습니다.");
        }

        // 새 비밀번호 유효성 검사
        if (!validationService.isValidPassword(request.getNewPassword())) {
            throw new RuntimeException("새 비밀번호 형식이 올바르지 않습니다.");
        }

        // 비밀번호 변경
        account.setPassword(authService.encryptPassword(request.getNewPassword()));
        accountRepository.save(account);
    }

    @Override
    public void deleteAccount(Long userId) {
        Account account = accountRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // Soft Delete 실행
        account.markDeleted();
        accountRepository.save(account);
    }

    // 헬퍼 메서드: Account를 UserProfileResponse로 변환
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
