package com.pickteam.service.user;

import com.pickteam.dto.user.UserLoginRequest;
import com.pickteam.dto.user.UserProfileResponse;
import com.pickteam.dto.user.LogoutResponse;
import com.pickteam.dto.user.SessionInfoRequest;
import com.pickteam.dto.security.JwtAuthenticationResponse;
import com.pickteam.dto.security.RefreshTokenRequest;
import com.pickteam.exception.user.UserNotFoundException;
import com.pickteam.exception.auth.InvalidTokenException;
import com.pickteam.exception.auth.AuthenticationException;
import com.pickteam.exception.auth.UnauthorizedException;
import com.pickteam.exception.auth.SessionExpiredException;
import com.pickteam.constants.AuthErrorMessages;
import com.pickteam.repository.user.AccountRepository;
import com.pickteam.repository.user.RefreshTokenRepository;
import com.pickteam.domain.user.Account;
import com.pickteam.domain.user.RefreshToken;
import com.pickteam.security.UserPrincipal;
import com.pickteam.service.security.SecurityAuditLogger;
import com.pickteam.util.ClientInfoExtractor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 인증 서비스 구현체
 * - JWT 토큰 생성/검증 및 사용자 인증 처리
 * - 비밀번호 암호화 및 검증
 * - 리프레시 토큰을 통한 토큰 갱신
 * - Spring Security와 연동한 현재 사용자 정보 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.pickteam.security.JwtTokenProvider jwtTokenProvider;
    private final SecurityAuditLogger securityAuditLogger;
    private final EmailService emailService; // 이메일 서비스 주입

    /** 리프레시 토큰 만료 기간 */
    @Value("${app.jwt.refresh-token.expiration-days}")
    private long refreshTokenExpirationDays;

    /**
     * 사용자 로그인 인증 처리
     * - 이메일/비밀번호 검증 후 JWT 토큰 발급
     * - Access Token과 Refresh Token 동시 생성
     * - 사용자 프로필 정보와 함께 응답 반환
     * 
     * @param request 로그인 요청 정보 (이메일, 비밀번호)
     * @return JWT 토큰과 사용자 정보가 포함된 인증 응답
     * @throws AuthenticationException 인증 실패 시 (이메일 또는 비밀번호 불일치)
     */
    @Override
    public JwtAuthenticationResponse authenticate(UserLoginRequest request) {
        log.info("사용자 로그인 시도: {}", request.getEmail());

        // 1. 사용자 조회
        Account account = accountRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() -> new AuthenticationException(AuthErrorMessages.INVALID_CREDENTIALS));

        // 2. 비밀번호 검증
        if (!matchesPassword(request.getPassword(), account.getPassword())) {
            log.warn("로그인 실패 - 비밀번호 불일치: {}", request.getEmail());
            throw new AuthenticationException(AuthErrorMessages.INVALID_CREDENTIALS);
        }

        // 3. 기존 세션 무효화 (중복 로그인 방지)
        invalidateExistingSessions(account);

        // 4. Access/Refresh 토큰 발급 (이름 포함)
        String accessToken = jwtTokenProvider.generateAccessToken(account.getId(), account.getEmail(),
                account.getName());
        String refreshToken = jwtTokenProvider.generateRefreshToken(account.getId());

        // 5. Refresh Token DB 저장
        createAndSaveRefreshToken(account, refreshToken);

        // 5. 사용자 정보 DTO 변환
        UserProfileResponse userProfile = mapToUserProfile(account);

        log.info("사용자 로그인 완료: {}", request.getEmail());

        // 6. 응답 반환
        return new JwtAuthenticationResponse(
                accessToken,
                refreshToken,
                jwtTokenProvider.getJwtExpirationMs(),
                userProfile);
    }

    /**
     * 비밀번호 암호화
     * - BCrypt 해시 알고리즘을 사용한 단방향 암호화
     * - Salt가 자동으로 생성되어 보안성 강화
     * 
     * @param password 평문 비밀번호
     * @return 암호화된 비밀번호
     */
    @Override
    public String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * 비밀번호 검증
     * - 평문 비밀번호와 암호화된 비밀번호 비교
     * - BCrypt의 matches 메서드를 통한 안전한 검증
     * 
     * @param rawPassword     평문 비밀번호
     * @param encodedPassword 암호화된 비밀번호
     * @return 비밀번호 일치 여부
     */
    @Override
    public boolean matchesPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * Access Token 생성 (이름 포함)
     * - 실시간 채팅 및 화상회의에서 사용자 이름 표시를 위해 토큰에 이름 정보 포함
     * - 짧은 만료시간을 가진 인증 토큰 생성
     * 
     * @param userId 사용자 ID
     * @param email  사용자 이메일
     * @param name   사용자 이름
     * @return 생성된 Access Token
     */
    @Override
    public String generateAccessToken(Long userId, String email, String name) {
        return jwtTokenProvider.generateAccessToken(userId, email, name);
    }

    /**
     * Refresh Token 생성 및 저장
     * - 긴 만료시간을 가진 토큰으로 Access Token 갱신에 사용
     * - MySQL DB에 영구 저장하여 다중 디바이스 지원
     * 
     * @param userId 사용자 ID
     * @return 생성된 Refresh Token
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Override
    public String generateRefreshToken(Long userId) {
        Account account = accountRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException(AuthErrorMessages.USER_NOT_FOUND));

        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

        // 기존 토큰 삭제 후 새 토큰 저장
        createAndSaveRefreshToken(account, refreshToken);

        log.info("리프레시 토큰 생성 완료: userId={}", userId);
        return refreshToken;
    }

    /**
     * JWT 토큰 유효성 검증
     * - 토큰 만료시간, 서명 등을 종합적으로 검증
     * 
     * @param token 검증할 JWT 토큰
     * @return 토큰 유효성 여부
     */
    @Override
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    /**
     * Refresh Token을 통한 새 토큰 발급
     * - Refresh Token 검증 후 새로운 Access/Refresh Token 쌍 생성
     * - 기존 Refresh Token은 무효화하고 새 토큰으로 교체
     * - 사용자 정보도 함께 반환
     * 
     * @param request Refresh Token 요청 정보
     * @return 새로운 JWT 토큰과 사용자 정보
     * @throws InvalidTokenException Refresh Token 검증 실패 시
     * @throws UserNotFoundException 사용자가 존재하지 않을 시
     */
    @Override
    @Transactional
    public JwtAuthenticationResponse refreshToken(RefreshTokenRequest request) {
        log.info("토큰 갱신 시도");

        // 1. DB에서 Refresh Token 조회 및 검증
        String refreshTokenValue = request.getRefreshToken();
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new InvalidTokenException(AuthErrorMessages.INVALID_REFRESH_TOKEN));

        // 2. 토큰 만료 확인
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidTokenException(AuthErrorMessages.EXPIRED_REFRESH_TOKEN);
        }

        // 3. JWT 토큰 자체 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshTokenValue)) {
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidTokenException(AuthErrorMessages.INVALID_REFRESH_TOKEN);
        }

        // 4. 사용자 정보 조회 및 세션 유효성 확인
        Account account = refreshToken.getAccount();
        if (account == null || account.getDeletedAt() != null) {
            refreshTokenRepository.delete(refreshToken);
            throw new SessionExpiredException("세션이 만료되었습니다. 다시 로그인해 주세요.");
        }

        // 4.5. 마지막 사용 시간 업데이트
        refreshToken.updateLastUsedTime();
        refreshTokenRepository.save(refreshToken);

        // 5. 새 Access/Refresh 토큰 발급 (이름 포함)
        String newAccessToken = jwtTokenProvider.generateAccessToken(account.getId(), account.getEmail(),
                account.getName());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(account.getId());

        // 6. 기존 토큰 삭제 후 새 토큰 저장
        createAndSaveRefreshToken(account, newRefreshToken);

        // 7. 사용자 정보 DTO 변환
        UserProfileResponse userProfile = mapToUserProfile(account);

        log.info("토큰 갱신 완료: userId={}", account.getId());

        // 8. 응답 반환
        return new JwtAuthenticationResponse(
                newAccessToken,
                newRefreshToken,
                jwtTokenProvider.getJwtExpirationMs(),
                userProfile);
    }

    /**
     * 현재 로그인된 사용자 ID 조회
     * - Spring Security Context에서 인증된 사용자 정보 추출
     * - JWT 토큰을 통해 인증된 사용자의 ID 반환
     * - 인증이 필요한 모든 API에서 사용하는 핵심 메서드
     * 
     * @return 현재 로그인된 사용자 ID
     * @throws AuthenticationException 인증되지 않은 사용자인 경우
     * @throws IllegalStateException   SecurityContext에 유효한 사용자 정보가 없는 경우
     */
    @Override
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException(AuthErrorMessages.AUTHENTICATION_REQUIRED);
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getId();
        }

        throw new IllegalStateException("SecurityContext에 유효한 사용자 정보가 없습니다.");
    }

    /**
     * 현재 사용자의 인증 상태를 확인하고 사용자 ID를 반환
     * - getCurrentUserId() 호출하여 인증 상태 확인
     * - 인증되지 않은 경우 UnauthorizedException 발생
     * - 컨트롤러에서 인증 로직 중복 제거를 위한 헬퍼 메서드
     * 
     * @return 인증된 사용자 ID
     * @throws UnauthorizedException 인증되지 않은 사용자인 경우
     */
    @Override
    public Long requireAuthentication() {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new UnauthorizedException(AuthErrorMessages.AUTHENTICATION_REQUIRED);
        }
        return currentUserId;
    }

    /**
     * Refresh Token 생성 및 저장 (중복 로직 제거)
     * - 기존 토큰 삭제 후 새 토큰 생성
     * - 설정 가능한 만료시간 설정
     * - MySQL DB에 영구 저장
     * 
     * @param account 토큰을 생성할 사용자 계정
     * @param token   저장할 토큰 문자열
     * @return 저장된 RefreshToken 엔티티
     */
    @Transactional
    private RefreshToken createAndSaveRefreshToken(Account account, String token) {
        refreshTokenRepository.deleteByAccount(account);
        LocalDateTime now = LocalDateTime.now();
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .account(account)
                .token(token)
                .expiresAt(now.plusDays(refreshTokenExpirationDays))
                .loginTime(now)
                .lastUsedTime(now)
                .ipAddress("Unknown") // 기본값 - 향후 개선 필요
                .deviceInfo("Legacy Login") // 기본값 - 향후 개선 필요
                .userAgent("Unknown") // 기본값 - 향후 개선 필요
                .invalidated(false)
                .build();
        return refreshTokenRepository.save(refreshTokenEntity);
    }

    /**
     * Account 엔티티를 UserProfileResponse DTO로 변환
     * - 중복 코드 제거를 위한 매핑 메서드
     * - 모든 사용자 프로필 정보를 DTO로 변환
     * 
     * @param account 변환할 사용자 계정 엔티티
     * @return 변환된 사용자 프로필 응답 DTO
     */
    private UserProfileResponse mapToUserProfile(Account account) {
        UserProfileResponse userProfile = new UserProfileResponse();
        userProfile.setId(account.getId());
        userProfile.setEmail(account.getEmail());
        userProfile.setName(account.getName());
        userProfile.setAge(account.getAge());
        userProfile.setRole(account.getRole());
        userProfile.setMbti(account.getMbti());
        userProfile.setDisposition(account.getDisposition());
        userProfile.setIntroduction(account.getIntroduction());
        userProfile.setPortfolio(account.getPortfolio());

        // 프로필 이미지 URL 설정 (null 그대로 반환 - 프론트엔드에서 처리)
        userProfile.setProfileImageUrl(account.getProfileImageUrl());

        userProfile.setPreferWorkstyle(account.getPreferWorkstyle());
        userProfile.setDislikeWorkstyle(account.getDislikeWorkstyle());
        return userProfile;
    }

    /**
     * 사용자 로그아웃 처리
     * - 해당 사용자의 모든 Refresh Token을 DB에서 삭제
     * - 토큰 무효화를 통한 보안 강화
     * - 다중 디바이스 로그아웃 지원
     * 
     * @param userId 로그아웃할 사용자 ID
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Override
    @Transactional
    public void logout(Long userId) {
        log.info("사용자 로그아웃 시작: userId={}", userId);

        // 1. 사용자 존재 확인
        Account account = accountRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException(AuthErrorMessages.USER_NOT_FOUND));

        // 2. 해당 사용자의 모든 Refresh Token 삭제
        refreshTokenRepository.deleteByAccount(account);

        log.info("사용자 로그아웃 완료: userId={}", userId);
    }

    /**
     * 개선된 사용자 로그아웃 처리
     * - 로그아웃 시간과 무효화된 세션 수 등 상세 정보 반환
     * - 해당 사용자의 모든 Refresh Token을 DB에서 삭제
     * - 보안 강화 및 사용자 피드백 개선
     * 
     * @param userId 로그아웃할 사용자 ID
     * @return 로그아웃 상세 정보
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Override
    @Transactional
    public LogoutResponse logoutWithDetails(Long userId) {
        log.info("개선된 사용자 로그아웃 시작: userId={}", userId);
        LocalDateTime logoutTime = LocalDateTime.now();

        // 1. 사용자 존재 확인
        Account account = accountRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException(AuthErrorMessages.USER_NOT_FOUND));

        // 2. 기존 Refresh Token 개수 확인
        List<RefreshToken> existingTokens = refreshTokenRepository.findByAccount(account);
        int tokenCount = existingTokens.size();

        // 3. 해당 사용자의 모든 Refresh Token 삭제
        refreshTokenRepository.deleteByAccount(account);

        log.info("개선된 사용자 로그아웃 완료: userId={}, 무효화된 세션 수={}", userId, tokenCount);

        return LogoutResponse.builder()
                .logoutTime(logoutTime)
                .invalidatedSessions(tokenCount)
                .message("로그아웃이 성공적으로 완료되었습니다.")
                .build();
    }

    /**
     * 기존 세션 무효화 (중복 로그인 방지)
     * - 해당 사용자의 모든 기존 Refresh Token을 삭제하여 세션 무효화
     * - 새 로그인 시 다른 기기에서의 로그인을 강제 종료
     * 
     * @param account 세션을 무효화할 사용자 계정
     */
    private void invalidateExistingSessions(Account account) {
        int deletedTokens = refreshTokenRepository.findByAccount(account).size();
        if (deletedTokens > 0) {
            refreshTokenRepository.deleteByAccount(account);
            log.info("기존 세션 무효화 완료: userId={}, 삭제된 토큰 수={}", account.getId(), deletedTokens);
        }
    }

    /**
     * 클라이언트 정보를 포함한 사용자 로그인 인증 처리
     */
    @Override
    @Transactional
    public JwtAuthenticationResponse authenticateWithClientInfo(UserLoginRequest request,
            SessionInfoRequest sessionInfo,
            HttpServletRequest httpRequest) {
        log.info("클라이언트 정보를 포함한 사용자 로그인 시작: email={}", request.getEmail());

        // 1. 기본 인증 수행
        Account account = accountRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(AuthErrorMessages.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            // 로그인 실패 로깅
            ClientInfoExtractor.ClientInfo clientInfo = ClientInfoExtractor.extractClientInfo(httpRequest);
            securityAuditLogger.logLoginFailure(request.getEmail(), clientInfo.getIpAddress(), "잘못된 비밀번호");
            throw new AuthenticationException(AuthErrorMessages.INVALID_CREDENTIALS);
        }

        // 2. 중복 로그인 방지 - 기존 세션 무효화
        List<RefreshToken> existingTokens = refreshTokenRepository.findByAccount(account);
        if (!existingTokens.isEmpty()) {
            ClientInfoExtractor.ClientInfo clientInfo = ClientInfoExtractor.extractClientInfo(httpRequest);
            securityAuditLogger.logDuplicateLogin(account, clientInfo.getIpAddress(),
                    clientInfo.getDeviceInfoString(), existingTokens.size());
            invalidateExistingSessions(account);
        }

        // 3. 새 토큰 생성 (이름 포함)
        String accessToken = generateAccessToken(account.getId(), account.getEmail(), account.getName());
        String refreshToken = generateRefreshTokenWithSessionInfo(account.getId(), sessionInfo, httpRequest);

        // 4. 로그인 성공 로깅
        RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalStateException("생성된 RefreshToken을 찾을 수 없습니다"));
        securityAuditLogger.logLoginSuccess(account, tokenEntity);

        log.info("클라이언트 정보를 포함한 사용자 로그인 완료: userId={}", account.getId());

        return new JwtAuthenticationResponse(
                accessToken,
                refreshToken,
                jwtTokenProvider.getJwtExpirationMs(),
                mapToUserProfile(account));
    }

    /**
     * 클라이언트 정보를 포함한 Refresh Token 생성
     */
    @Override
    @Transactional
    public String generateRefreshTokenWithClientInfo(Long userId, ClientInfoExtractor.ClientInfo clientInfo) {
        log.info("클라이언트 정보를 포함한 Refresh Token 생성 시작: userId={}", userId);

        Account account = accountRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException(AuthErrorMessages.USER_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        String tokenValue = jwtTokenProvider.generateRefreshToken(userId);

        RefreshToken refreshToken = RefreshToken.builder()
                .account(account)
                .token(tokenValue)
                .expiresAt(now.plusDays(refreshTokenExpirationDays))
                .loginTime(now)
                .lastUsedTime(now)
                .ipAddress(clientInfo.getIpAddress())
                .deviceInfo(clientInfo.getDeviceInfoString())
                .userAgent(clientInfo.getUserAgent())
                .invalidated(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        log.info("클라이언트 정보를 포함한 Refresh Token 생성 완료: userId={}", userId);

        return tokenValue;
    }

    /**
     * 세션 정보를 포함한 Refresh Token 생성
     */
    @Override
    @Transactional
    public String generateRefreshTokenWithSessionInfo(Long userId, SessionInfoRequest sessionInfo,
            HttpServletRequest httpRequest) {
        log.info("세션 정보를 포함한 Refresh Token 생성 시작: userId={}", userId);

        Account account = accountRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException(AuthErrorMessages.USER_NOT_FOUND));

        ClientInfoExtractor.ClientInfo clientInfo = ClientInfoExtractor.extractClientInfo(httpRequest);
        LocalDateTime now = LocalDateTime.now();
        String tokenValue = jwtTokenProvider.generateRefreshToken(userId);

        // 세션 정보와 클라이언트 정보 조합
        String deviceInfo = sessionInfo != null && sessionInfo.toDeviceInfoString() != null
                ? sessionInfo.toDeviceInfoString()
                : clientInfo.getDeviceInfoString();

        RefreshToken refreshToken = RefreshToken.builder()
                .account(account)
                .token(tokenValue)
                .expiresAt(now.plusDays(refreshTokenExpirationDays))
                .loginTime(now)
                .lastUsedTime(now)
                .ipAddress(clientInfo.getIpAddress())
                .deviceInfo(deviceInfo)
                .userAgent(clientInfo.getUserAgent())
                .invalidated(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        log.info("세션 정보를 포함한 Refresh Token 생성 완료: userId={}", userId);

        return tokenValue;
    }

    /**
     * 클라이언트 정보를 포함한 상세 로그아웃 처리
     */
    @Override
    @Transactional
    public LogoutResponse logoutWithDetails(Long userId, HttpServletRequest httpRequest) {
        log.info("클라이언트 정보를 포함한 사용자 로그아웃 시작: userId={}", userId);
        LocalDateTime logoutTime = LocalDateTime.now();

        // 1. 사용자 존재 확인
        Account account = accountRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException(AuthErrorMessages.USER_NOT_FOUND));

        // 2. 기존 Refresh Token 개수 확인 및 로깅
        List<RefreshToken> existingTokens = refreshTokenRepository.findByAccount(account);
        int tokenCount = existingTokens.size();

        // 3. 로그아웃 로깅
        ClientInfoExtractor.ClientInfo clientInfo = ClientInfoExtractor.extractClientInfo(httpRequest);
        securityAuditLogger.logLogout(account, clientInfo.getIpAddress(), tokenCount);

        // 4. 해당 사용자의 모든 Refresh Token 삭제
        refreshTokenRepository.deleteByAccount(account);

        log.info("클라이언트 정보를 포함한 사용자 로그아웃 완료: userId={}, 무효화된 세션 수={}", userId, tokenCount);

        return LogoutResponse.builder()
                .logoutTime(logoutTime)
                .invalidatedSessions(tokenCount)
                .message("로그아웃이 성공적으로 완료되었습니다.")
                .build();
    }

    // === OAuth 전용 토큰 생성 메서드 ===

    @Override
    @Transactional
    public JwtAuthenticationResponse generateTokensForAccount(Account account) {
        log.debug("Account 객체로부터 JWT 토큰 생성 시작 - 사용자 ID: {}", account.getId());

        if (account == null) {
            throw new IllegalArgumentException("Account는 null일 수 없습니다");
        }

        if (account.getId() == null || account.getEmail() == null) {
            throw new IllegalArgumentException("Account의 ID와 이메일은 필수입니다");
        }

        try {
            // Access Token 생성
            String accessToken = generateAccessToken(account.getId(), account.getEmail(), account.getName());

            // Refresh Token 생성
            String refreshToken = generateRefreshToken(account.getId());

            // 사용자 프로필 정보 생성
            UserProfileResponse userProfile = mapToUserProfile(account);

            log.info("Account 객체로부터 JWT 토큰 생성 완료 - 사용자 ID: {}", account.getId());

            return JwtAuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getJwtExpirationMs())
                    .user(userProfile)
                    .build();

        } catch (Exception e) {
            log.error("Account 객체로부터 JWT 토큰 생성 실패 - 사용자 ID: {}", account.getId(), e);
            throw new RuntimeException("JWT 토큰 생성에 실패했습니다", e);
        }
    }

    @Override
    @Transactional
    public JwtAuthenticationResponse generateTokensForAccount(Account account, HttpServletRequest httpRequest) {
        log.debug("클라이언트 정보를 포함하여 Account 객체로부터 JWT 토큰 생성 시작 - 사용자 ID: {}", account.getId());

        if (account == null) {
            throw new IllegalArgumentException("Account는 null일 수 없습니다");
        }

        if (account.getId() == null || account.getEmail() == null) {
            throw new IllegalArgumentException("Account의 ID와 이메일은 필수입니다");
        }

        try {
            // Access Token 생성
            String accessToken = generateAccessToken(account.getId(), account.getEmail(), account.getName());

            // 클라이언트 정보 추출
            ClientInfoExtractor.ClientInfo clientInfo = null;
            if (httpRequest != null) {
                clientInfo = ClientInfoExtractor.extractClientInfo(httpRequest);
            }

            // Refresh Token 생성 (클라이언트 정보 포함)
            String refreshToken;
            if (clientInfo != null) {
                refreshToken = generateRefreshTokenWithClientInfo(account.getId(), clientInfo);
            } else {
                refreshToken = generateRefreshToken(account.getId());
            }

            log.info("클라이언트 정보를 포함하여 Account 객체로부터 JWT 토큰 생성 완료 - 사용자 ID: {}", account.getId());

            // 사용자 프로필 정보 생성
            UserProfileResponse userProfile = mapToUserProfile(account);

            return JwtAuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getJwtExpirationMs())
                    .user(userProfile)
                    .build();

        } catch (Exception e) {
            log.error("클라이언트 정보를 포함한 Account 객체로부터 JWT 토큰 생성 실패 - 사용자 ID: {}", account.getId(), e);
            throw new RuntimeException("JWT 토큰 생성에 실패했습니다", e);
        }
    }

    // ==================== 비밀번호 찾기 관련 메서드 구현 ====================

    @Override
    @Transactional
    public void sendPasswordResetEmail(String email) {
        log.info("비밀번호 재설정 이메일 발송 시작 - 이메일: {}", maskEmail(email));

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수입니다");
        }

        try {
            // 계정 존재 여부 확인 (보안상 존재하지 않아도 성공 반환)
            Account account = accountRepository.findByEmail(email).orElse(null);

            if (account != null) {
                // 비밀번호 재설정용 인증 코드 생성
                String resetCode = emailService.generateVerificationCode();

                // 인증 코드 저장 (기존 이메일 인증 시스템 재활용)
                emailService.storeVerificationCode(email, resetCode);

                // 비밀번호 재설정 이메일 발송
                emailService.sendPasswordResetEmail(email, resetCode);

                log.info("등록된 계정으로 비밀번호 재설정 코드 발송 완료 - 사용자 ID: {}", account.getId());

            } else {
                log.warn("존재하지 않는 이메일로 비밀번호 재설정 요청 - 이메일: {}", maskEmail(email));
                // 보안상 계정 존재 여부를 노출하지 않음
            }

            log.info("비밀번호 재설정 이메일 발송 완료 - 이메일: {}", maskEmail(email));

        } catch (Exception e) {
            log.error("비밀번호 재설정 이메일 발송 실패 - 이메일: {}", maskEmail(email), e);
            // 보안상 구체적인 오류 정보는 숨김
            throw new RuntimeException("이메일 발송 중 오류가 발생했습니다");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifyPasswordResetCode(String email, String resetCode) {
        log.info("비밀번호 재설정 코드 검증 시작 - 이메일: {}", maskEmail(email));

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수입니다");
        }
        if (resetCode == null || resetCode.trim().isEmpty()) {
            throw new IllegalArgumentException("재설정 코드는 필수입니다");
        }

        try {
            // 계정 존재 여부 확인
            Account account = accountRepository.findByEmail(email).orElse(null);

            if (account == null) {
                log.warn("존재하지 않는 이메일로 재설정 코드 검증 시도 - 이메일: {}", maskEmail(email));
                return false;
            }

            // 기존 이메일 인증 시스템의 코드 검증 로직 재활용
            boolean isValid = emailService.verifyCode(email, resetCode);

            log.info("비밀번호 재설정 코드 검증 결과 - 이메일: {}, 유효성: {}", maskEmail(email), isValid);
            return isValid;

        } catch (Exception e) {
            log.error("비밀번호 재설정 코드 검증 실패 - 이메일: {}", maskEmail(email), e);
            return false;
        }
    }

    @Override
    @Transactional
    public void resetPasswordWithCode(String email, String resetCode, String newPassword) {
        log.info("비밀번호 재설정 시작 - 이메일: {}", maskEmail(email));

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수입니다");
        }
        if (resetCode == null || resetCode.trim().isEmpty()) {
            throw new IllegalArgumentException("재설정 코드는 필수입니다");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("새 비밀번호는 필수입니다");
        }

        try {
            // 계정 조회
            Account account = accountRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("계정을 찾을 수 없습니다"));

            // 재설정 코드 재검증
            if (!verifyPasswordResetCode(email, resetCode)) {
                log.warn("유효하지 않은 재설정 코드로 비밀번호 변경 시도 - 이메일: {}", maskEmail(email));
                throw new InvalidTokenException("재설정 코드가 유효하지 않거나 만료되었습니다");
            }

            // 새 비밀번호 암호화
            String encodedNewPassword = encryptPassword(newPassword);

            // 비밀번호 업데이트
            account.setPassword(encodedNewPassword);
            accountRepository.save(account);

            // 보안을 위해 해당 사용자의 모든 리프레시 토큰 무효화
            List<RefreshToken> userTokens = refreshTokenRepository.findByAccount(account);
            refreshTokenRepository.deleteAll(userTokens);
            log.info("비밀번호 변경으로 인한 기존 세션 무효화 - 사용자 ID: {}, 무효화된 토큰 수: {}",
                    account.getId(), userTokens.size());

            // TODO: 사용된 재설정 코드 무효화
            // - Redis 또는 DB에서 해당 재설정 코드 삭제/무효화

            // 보안 감사 로그 (메서드가 없는 경우 주석 처리)
            // securityAuditLogger.logPasswordReset(account.getId(), account.getEmail());

            log.info("비밀번호 재설정 완료 - 사용자 ID: {}", account.getId());

        } catch (UserNotFoundException | InvalidTokenException e) {
            log.warn("비밀번호 재설정 실패 - 이메일: {}, 오류: {}", maskEmail(email), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("비밀번호 재설정 중 예상치 못한 오류 - 이메일: {}", maskEmail(email), e);
            throw new RuntimeException("비밀번호 재설정 중 오류가 발생했습니다", e);
        }
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
