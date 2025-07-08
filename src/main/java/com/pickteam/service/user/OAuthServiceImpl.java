package com.pickteam.service.user;

import com.pickteam.domain.enums.AuthProvider;
import com.pickteam.domain.user.Account;
import com.pickteam.dto.security.JwtAuthenticationResponse;
import com.pickteam.dto.user.OAuthUserInfo;
import com.pickteam.exception.user.DuplicateEmailException;
import com.pickteam.exception.user.UserNotFoundException;
import com.pickteam.repository.user.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * OAuth 통합 서비스 구현체
 * - 구글, 카카오 OAuth 서비스를 통합 관리
 * - OAuth 로그인 플로우와 자동 회원가입 처리
 * - 계정 연동/해제 기능 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OAuthServiceImpl implements OAuthService {

    private final GoogleOAuthService googleOAuthService;
    private final KakaoOAuthService kakaoOAuthService;
    private final AccountRepository accountRepository;
    private final AuthService authService;

    @Override
    public String generateOAuthUrl(AuthProvider provider) {
        log.debug("OAuth URL 생성 요청 - 제공자: {}", provider);

        switch (provider) {
            case GOOGLE:
                return googleOAuthService.generateOAuthUrl();
            case KAKAO:
                return kakaoOAuthService.generateOAuthUrl();
            default:
                throw new IllegalArgumentException("지원하지 않는 OAuth 제공자입니다: " + provider);
        }
    }

    @Override
    public JwtAuthenticationResponse processOAuthLogin(AuthProvider provider, String code) {
        log.info("OAuth 로그인 처리 시작 - 제공자: {}", provider);

        try {
            // 1. OAuth 사용자 정보 조회
            OAuthUserInfo oauthUserInfo = getOAuthUserInfo(provider, code);
            log.debug("OAuth 사용자 정보 조회 완료 - 이메일: {}", maskEmail(oauthUserInfo.getEmail()));

            // 2. 기존 계정 확인 (이메일 기준)
            Optional<Account> existingAccount = findAccountByEmail(oauthUserInfo.getEmail());

            Account account;
            boolean isFirstLogin = false;

            if (existingAccount.isPresent()) {
                // 3-1. 기존 사용자 - OAuth 정보 업데이트
                account = existingAccount.get();
                log.info("기존 사용자 OAuth 로그인 - 사용자 ID: {}", account.getId());

                updateAccountWithOAuthInfo(account, oauthUserInfo);
            } else {
                // 3-2. 신규 사용자 - 자동 회원가입
                log.info("신규 사용자 OAuth 회원가입 시작 - 이메일: {}", maskEmail(oauthUserInfo.getEmail()));

                account = createAccountFromOAuth(oauthUserInfo);
                isFirstLogin = true;

                log.info("신규 사용자 OAuth 회원가입 완료 - 사용자 ID: {}", account.getId());
            }

            // 4. JWT 토큰 발급
            JwtAuthenticationResponse jwtResponse = generateJwtTokens(account);

            // 5. 첫 로그인 여부 설정 (클라이언트에서 온보딩 등에 활용)
            // JwtAuthenticationResponse에 isFirstLogin 필드가 있다면 설정

            log.info("OAuth 로그인 완료 - 사용자 ID: {}, 첫 로그인: {}", account.getId(), isFirstLogin);
            return jwtResponse;

        } catch (Exception e) {
            log.error("OAuth 로그인 처리 중 오류 발생 - 제공자: {}", provider, e);
            throw new RuntimeException("OAuth 로그인 처리 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public void linkOAuthAccount(Long userId, AuthProvider provider, String code) {
        log.info("OAuth 계정 연동 시작 - 사용자 ID: {}, 제공자: {}", userId, provider);

        try {
            // 1. 사용자 계정 조회
            Account account = accountRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));

            // 2. 이미 해당 제공자로 연동되어 있는지 확인
            if (account.getProvider() == provider && account.getProviderId() != null) {
                throw new RuntimeException("이미 " + provider.getDescription() + " 계정이 연동되어 있습니다");
            }

            // 3. OAuth 사용자 정보 조회
            OAuthUserInfo oauthUserInfo = getOAuthUserInfo(provider, code);

            // 4. 다른 계정이 해당 OAuth 계정을 사용하고 있는지 확인
            Optional<Account> existingOAuthAccount = accountRepository.findByProviderAndProviderId(
                    provider, oauthUserInfo.getProviderId());

            if (existingOAuthAccount.isPresent() && !existingOAuthAccount.get().getId().equals(userId)) {
                throw new RuntimeException("해당 " + provider.getDescription() + " 계정은 이미 다른 사용자가 사용 중입니다");
            }

            // 5. OAuth 정보 연동
            updateAccountWithOAuthInfo(account, oauthUserInfo);

            log.info("OAuth 계정 연동 완료 - 사용자 ID: {}, 제공자: {}", userId, provider);

        } catch (Exception e) {
            log.error("OAuth 계정 연동 중 오류 발생 - 사용자 ID: {}, 제공자: {}", userId, provider, e);
            throw new RuntimeException("OAuth 계정 연동 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public void unlinkOAuthAccount(Long userId, AuthProvider provider) {
        log.info("OAuth 계정 연동 해제 시작 - 사용자 ID: {}, 제공자: {}", userId, provider);

        try {
            // 1. 사용자 계정 조회
            Account account = accountRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));

            // 2. 해당 제공자로 연동되어 있는지 확인
            if (account.getProvider() != provider || account.getProviderId() == null) {
                throw new RuntimeException(provider.getDescription() + " 계정이 연동되어 있지 않습니다");
            }

            // 3. 로컬 계정으로만 로그인 가능한지 확인 (비밀번호 설정 여부)
            if (account.getPassword() == null || account.getPassword().trim().isEmpty()) {
                throw new RuntimeException("비밀번호가 설정되지 않은 상태에서는 소셜 로그인 연동을 해제할 수 없습니다. 먼저 비밀번호를 설정해주세요.");
            }

            // 4. OAuth 연동 해제
            account.unlinkOAuth();
            accountRepository.save(account);

            log.info("OAuth 계정 연동 해제 완료 - 사용자 ID: {}, 제공자: {}", userId, provider);

        } catch (Exception e) {
            log.error("OAuth 계정 연동 해제 중 오류 발생 - 사용자 ID: {}, 제공자: {}", userId, provider, e);
            throw new RuntimeException("OAuth 계정 연동 해제 실패: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOAuthLinked(Long userId, AuthProvider provider) {
        return accountRepository.findById(userId)
                .map(account -> account.getProvider() == provider && account.getProviderId() != null)
                .orElse(false);
    }

    // === Private Helper Methods ===

    /**
     * OAuth 제공자별 사용자 정보 조회
     */
    private OAuthUserInfo getOAuthUserInfo(AuthProvider provider, String code) {
        switch (provider) {
            case GOOGLE:
                String googleAccessToken = googleOAuthService.getAccessToken(code);
                return googleOAuthService.getUserInfo(googleAccessToken);
            case KAKAO:
                String kakaoAccessToken = kakaoOAuthService.getAccessToken(code);
                return kakaoOAuthService.getUserInfo(kakaoAccessToken);
            default:
                throw new IllegalArgumentException("지원하지 않는 OAuth 제공자입니다: " + provider);
        }
    }

    /**
     * 이메일로 계정 조회 (소프트 삭제된 계정 제외)
     */
    private Optional<Account> findAccountByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        return accountRepository.findByEmailAndDeletedAtIsNull(email);
    }

    /**
     * OAuth 정보로 신규 계정 생성
     */
    private Account createAccountFromOAuth(OAuthUserInfo oauthUserInfo) {
        // 이메일 중복 체크 (혹시나 하는 추가 안전장치)
        if (accountRepository.existsByEmail(oauthUserInfo.getEmail())) {
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다: " + oauthUserInfo.getEmail());
        }

        Account account = Account.builder()
                .email(oauthUserInfo.getEmail())
                .name(oauthUserInfo.getDisplayName())
                .password("") // OAuth 사용자는 비밀번호 없음
                .provider(oauthUserInfo.getProvider())
                .providerId(oauthUserInfo.getProviderId())
                .socialEmail(oauthUserInfo.getEmail())
                .socialName(oauthUserInfo.getName())
                .socialProfileUrl(oauthUserInfo.getProfileImageUrl())
                .profileImageUrl(oauthUserInfo.getProfileImageUrl()) // 소셜 프로필 이미지를 기본 프로필로 설정
                .build();

        return accountRepository.save(account);
    }

    /**
     * 기존 계정에 OAuth 정보 업데이트
     */
    private void updateAccountWithOAuthInfo(Account account, OAuthUserInfo oauthUserInfo) {
        account.updateOAuthInfo(
                oauthUserInfo.getProvider(),
                oauthUserInfo.getProviderId(),
                oauthUserInfo.getEmail(),
                oauthUserInfo.getName(),
                oauthUserInfo.getProfileImageUrl());
        accountRepository.save(account);
    }

    /**
     * JWT 토큰 발급
     */
    private JwtAuthenticationResponse generateJwtTokens(Account account) {
        // 기존 AuthService의 토큰 발급 로직 재사용
        // UserLoginRequest 없이 직접 Account로 토큰 발급하는 메서드가 필요할 수 있음
        // 임시로 이메일과 빈 비밀번호로 로그인 시뮬레이션
        try {
            return authService.generateTokensForAccount(account);
        } catch (Exception e) {
            log.error("JWT 토큰 발급 실패 - 사용자 ID: {}", account.getId(), e);
            throw new RuntimeException("로그인 토큰 발급에 실패했습니다", e);
        }
    }

    /**
     * 이메일 마스킹 (로그용)
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        if (localPart.length() <= 2) {
            return "***@" + domain;
        }

        return localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1) + "@" + domain;
    }
}
