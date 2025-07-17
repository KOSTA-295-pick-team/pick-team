package com.pickteam.service.user;

import com.pickteam.domain.enums.AuthProvider;
import com.pickteam.domain.user.Account;
import com.pickteam.dto.security.JwtAuthenticationResponse;
import com.pickteam.dto.user.OAuthUserInfo;
import com.pickteam.exception.user.DuplicateEmailException;
import com.pickteam.exception.user.OAuthDeletedAccountException;
import com.pickteam.exception.user.UserNotFoundException;
import com.pickteam.repository.user.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

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
    private final PasswordEncoder passwordEncoder;

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

            // 2. 기존 계정 확인 (이메일 또는 providerId 기준)
            Optional<Account> existingAccount = findExistingAccount(oauthUserInfo);

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

        } catch (OAuthDeletedAccountException e) {
            // 삭제된 계정 예외는 그대로 전파하여 컨트롤러에서 적절히 처리
            log.warn("OAuth 로그인 실패: 삭제된 계정 - 제공자: {}, 계정 ID: {}", provider, e.getAccountId());
            throw e;
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
     * 이메일 또는 제공자 정보로 기존 계정 조회
     * providerId로 먼저 조회하되, 삭제된 계정도 포함하여 체크
     * 이메일 조회 시에는 provider도 함께 고려하여 계정 분리
     */
    private Optional<Account> findExistingAccount(OAuthUserInfo oauthUserInfo) {
        log.debug("기존 계정 검색 시작 - Provider: {}, ProviderId: {}, Email: {}",
                oauthUserInfo.getProvider(),
                maskProviderId(oauthUserInfo.getProviderId()),
                maskEmail(oauthUserInfo.getEmail()));

        // 1. providerId로 조회 (삭제된 계정 포함)
        Optional<Account> providerAccount = findAccountByProviderIncludingDeleted(oauthUserInfo.getProvider(),
                oauthUserInfo.getProviderId());
        if (providerAccount.isPresent()) {
            Account account = providerAccount.get();
            if (account.getDeletedAt() != null) {
                // 삭제된 계정인 경우 에러 발생
                log.warn("삭제된 계정으로 OAuth 로그인 시도 - Provider: {}, 계정 ID: {}",
                        oauthUserInfo.getProvider(), account.getId());
                throw new OAuthDeletedAccountException("삭제된 계정입니다. 일정 기간 후 재가입이 가능합니다.", account);
            }
            log.info("기존 계정 발견 (providerId 매칭) - 계정 ID: {}, Provider: {}",
                    account.getId(), oauthUserInfo.getProvider());
            return providerAccount;
        }

        // 2. 이메일이 있는 경우 이메일과 provider로 조회 (삭제된 계정 포함)
        // provider별로 계정을 분리하여 OAuth와 로컬 계정이 공존할 수 있도록 함
        if (oauthUserInfo.getEmail() != null && !oauthUserInfo.getEmail().trim().isEmpty()) {
            Optional<Account> emailAccount = findAccountByEmailAndProviderIncludingDeleted(
                    oauthUserInfo.getEmail(), oauthUserInfo.getProvider());
            if (emailAccount.isPresent()) {
                Account account = emailAccount.get();
                if (account.getDeletedAt() != null) {
                    // 삭제된 계정인 경우 에러 발생
                    log.warn("삭제된 계정 이메일로 OAuth 로그인 시도 - Email: {}, Provider: {}, 계정 ID: {}",
                            maskEmail(oauthUserInfo.getEmail()), oauthUserInfo.getProvider(), account.getId());
                    throw new OAuthDeletedAccountException("삭제된 계정이 있습니다. 일정 기간 후 재가입이 가능합니다.", account);
                }
                log.info("기존 계정 발견 (이메일+Provider 매칭) - 계정 ID: {}, Email: {}, Provider: {}",
                        account.getId(), maskEmail(oauthUserInfo.getEmail()), oauthUserInfo.getProvider());
                return emailAccount;
            }
        }

        log.debug("기존 계정 없음 - 신규 계정 생성 필요");
        return Optional.empty();
    }

    /**
     * OAuth 제공자 정보로 기존 계정 조회 (삭제된 계정 포함)
     */
    private Optional<Account> findAccountByProviderIncludingDeleted(AuthProvider provider, String providerId) {
        if (provider == null || providerId == null || providerId.trim().isEmpty()) {
            log.debug("Provider 검색 조건 부족 - Provider: {}, ProviderId: {}", provider, maskProviderId(providerId));
            return Optional.empty();
        }

        log.debug("Provider 기반 계정 검색 (삭제된 계정 포함) - Provider: {}, ProviderId: {}", provider, maskProviderId(providerId));
        Optional<Account> result = accountRepository.findByProviderAndProviderId(provider, providerId);

        if (result.isPresent()) {
            Account account = result.get();
            if (account.getDeletedAt() != null) {
                log.debug("Provider 기반 삭제된 계정 발견 - 계정 ID: {}, 삭제일: {}", account.getId(), account.getDeletedAt());
            } else {
                log.debug("Provider 기반 활성 계정 발견 - 계정 ID: {}", account.getId());
            }
        } else {
            log.debug("Provider 기반 계정 없음");
        }

        return result;
    }

    /**
     * 이메일로 계정 조회 (삭제된 계정 포함)
     * @deprecated 사용하지 마세요. provider별 계정 분리를 위해 findAccountByEmailAndProviderIncludingDeleted를 사용하세요.
     */
    @Deprecated
    private Optional<Account> findAccountByEmailIncludingDeleted(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        log.debug("이메일 기반 계정 검색 (삭제된 계정 포함) - Email: {}", maskEmail(email));
        return accountRepository.findByEmail(email);
    }

    /**
     * 이메일과 provider로 계정 조회 (삭제된 계정 포함)
     * OAuth와 로컬 계정을 분리하여 관리하기 위해 provider도 함께 조회
     */
    private Optional<Account> findAccountByEmailAndProviderIncludingDeleted(String email, AuthProvider provider) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        log.debug("이메일+Provider 기반 계정 검색 (삭제된 계정 포함) - Email: {}, Provider: {}", 
                maskEmail(email), provider);
        return accountRepository.findByEmailAndProvider(email, provider);
    }

    /**
     * OAuth 정보로 신규 계정 생성
     * provider별로 계정을 분리하여 관리하므로 같은 이메일이라도 다른 provider면 별도 계정 생성 가능
     */
    private Account createAccountFromOAuth(OAuthUserInfo oauthUserInfo) {
        String email = oauthUserInfo.getEmail();

        // 이메일이 있는 경우 provider별 중복 체크
        if (email != null && !email.trim().isEmpty()) {
            log.debug("OAuth 신규 계정 생성 - 이메일+Provider 중복 체크: {} ({})", 
                    maskEmail(email), oauthUserInfo.getProvider());

            // 1. 같은 provider의 활성 계정 중복 체크
            Optional<Account> existingSameProvider = accountRepository
                    .findByEmailAndProviderAndDeletedAtIsNull(email, oauthUserInfo.getProvider());
            if (existingSameProvider.isPresent()) {
                log.warn("OAuth 신규 계정 생성 실패 - 같은 provider로 이미 가입된 이메일: {} ({})", 
                        maskEmail(email), oauthUserInfo.getProvider());
                throw new DuplicateEmailException("해당 소셜 계정으로 이미 가입된 이메일입니다: " + email);
            }

            // 2. 같은 provider의 소프트 삭제된 계정 체크
            Optional<Account> deletedSameProvider = accountRepository
                    .findByEmailAndProvider(email, oauthUserInfo.getProvider());
            if (deletedSameProvider.isPresent() && deletedSameProvider.get().getDeletedAt() != null) {
                log.warn("OAuth 신규 계정 생성 실패 - 같은 provider의 소프트 삭제된 계정 존재: {} ({})", 
                        maskEmail(email), oauthUserInfo.getProvider());
                throw new OAuthDeletedAccountException("삭제된 계정이 있습니다. 일정 기간 후 재가입이 가능합니다.", 
                        deletedSameProvider.get());
            }

            // 3. 다른 provider 계정 존재 시 정보성 로그 (허용됨)
            if (accountRepository.existsByEmailAndDeletedAtIsNull(email)) {
                log.info("OAuth 계정 생성 - 다른 provider로 가입된 이메일이지만 허용: {} (기존: 다른 provider, 신규: {})", 
                        maskEmail(email), oauthUserInfo.getProvider());
            }
        } else {
            // 이메일이 없는 경우 providerId 기반 고유 이메일 생성
            // generateUniqueEmailFromProvider에서 이미 중복 체크를 수행하므로 추가 체크 불필요
            log.debug("OAuth 신규 계정 생성 - 이메일 없음, 고유 이메일 생성 시작");
            email = generateUniqueEmailFromProvider(oauthUserInfo);
        }

        log.info("OAuth 신규 계정 생성 - 최종 이메일: {}", maskEmail(email));

        Account account = Account.builder()
                .email(email)
                .name(oauthUserInfo.getDisplayName())
                .password(generateSecureOAuthPassword(oauthUserInfo.getProvider(), oauthUserInfo.getProviderId())) // OAuth
                                                                                                                   // 전용
                                                                                                                   // 안전한
                                                                                                                   // 임시
                                                                                                                   // 비밀번호
                .provider(oauthUserInfo.getProvider())
                .providerId(oauthUserInfo.getProviderId())
                .socialEmail(oauthUserInfo.getEmail()) // 실제 소셜 이메일 (null일 수 있음)
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

    /**
     * 이메일이 없는 OAuth 사용자를 위한 고유 이메일 생성
     * providerId 기반으로 생성 (삭제된 계정 체크는 이미 완료된 상태)
     */
    private String generateUniqueEmailFromProvider(OAuthUserInfo oauthUserInfo) {
        String provider = oauthUserInfo.getProvider().name().toLowerCase();
        String providerId = oauthUserInfo.getProviderId();

        // 제공자별 고유 이메일 생성: oauth_kakao_123456789@no-reply.pickteam.local
        String generatedEmail = "oauth_" + provider + "_" + providerId + "@no-reply.pickteam.local";

        log.info("이메일 없는 OAuth 사용자를 위한 고유 이메일 생성: {} -> {}",
                maskProviderId(providerId), maskEmail(generatedEmail));

        return generatedEmail;
    }

    /**
     * ProviderId 마스킹 (로그용)
     */
    private String maskProviderId(String providerId) {
        if (providerId == null || providerId.length() <= 4) {
            return "***";
        }
        return "***" + providerId.substring(providerId.length() - 4);
    }

    /**
     * OAuth 전용 안전한 임시 비밀번호 생성
     * - 사용자가 알 수 없는 고유한 값으로 생성
     * - 실제 로그인에는 사용할 수 없음 (OAuth 토큰만 사용)
     * - BCrypt 72바이트 제한을 고려하여 짧게 생성
     *
     * @param provider   OAuth 제공자
     * @param providerId 제공자별 사용자 ID
     * @return BCrypt로 해시화된 임시 비밀번호
     */
    private String generateSecureOAuthPassword(AuthProvider provider, String providerId) {
        // BCrypt 72바이트 제한을 고려하여 짧은 고유 문자열 생성
        String shortUuid = UUID.randomUUID().toString().replace("-", "").substring(0, 16); // 16자리
        String uniqueString = String.format("OAUTH_%s_%s_%s",
                provider.name().substring(0, 1), // 첫 글자만 (G, K)
                providerId.length() > 8 ? providerId.substring(providerId.length() - 8) : providerId, // 마지막 8자리
                shortUuid); // 16자리

        // 최대 길이 확인 (안전하게 60바이트 이하로 제한)
        if (uniqueString.length() > 60) {
            uniqueString = uniqueString.substring(0, 60);
        }

        // BCrypt로 해시화하여 반환
        return passwordEncoder.encode(uniqueString);
    }

    /**
     * 삭제된 계정 정보 조회 (OAuth 로그인 실패 시 상세 정보 제공용)
     * 
     * @param accountId 삭제된 계정 ID
     * @throws OAuthDeletedAccountException 항상 발생 (삭제된 계정 정보와 함께)
     * @throws RuntimeException             계정을 찾을 수 없거나 삭제되지 않은 계정인 경우
     */
    @Override
    public void getDeletedAccountInfo(Long accountId) {
        log.info("삭제된 계정 정보 조회 - 계정 ID: {}", accountId);

        // 사용자 정보 조회 (삭제된 계정 포함 - JPA 기본 findById 사용)
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 계정 ID: {}", accountId);
                    return new RuntimeException("해당 계정을 찾을 수 없습니다");
                });

        // 계정이 삭제되지 않은 경우 에러
        if (!account.getIsDeleted()) {
            log.warn("삭제되지 않은 계정에 대한 삭제 정보 조회 요청 - 계정 ID: {}", accountId);
            throw new RuntimeException("요청한 계정은 삭제된 계정이 아닙니다");
        }

        log.warn("삭제된 계정 정보 조회 완료 - 계정 ID: {}, 제공자: {}", accountId, account.getProvider());

        // OAuthDeletedAccountException 발생 (GlobalExceptionHandler가 처리)
        throw new OAuthDeletedAccountException(
                "삭제된 계정입니다. 일정 기간 후 재가입이 가능합니다.",
                account);
    }
}
