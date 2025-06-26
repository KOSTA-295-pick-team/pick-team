package com.pickteam.service.user;

import com.pickteam.dto.user.UserLoginRequest;
import com.pickteam.dto.user.UserProfileResponse;
import com.pickteam.dto.security.JwtAuthenticationResponse;
import com.pickteam.dto.security.RefreshTokenRequest;
import com.pickteam.repository.user.AccountRepository;
import com.pickteam.repository.user.RefreshTokenRepository;
import com.pickteam.domain.user.Account;
import com.pickteam.domain.user.RefreshToken;
import com.pickteam.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;

/**
 * 인증 서비스 구현체
 * - JWT 토큰 생성/검증 및 사용자 인증 처리
 * - 비밀번호 암호화 및 검증
 * - 리프레시 토큰을 통한 토큰 갱신
 * - Spring Security와 연동한 현재 사용자 정보 조회
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final com.pickteam.security.JwtTokenProvider jwtTokenProvider;

    /**
     * 사용자 로그인 인증 처리
     * - 이메일/비밀번호 검증 후 JWT 토큰 발급
     * - Access Token과 Refresh Token 동시 생성
     * - 사용자 프로필 정보와 함께 응답 반환
     * 
     * @param request 로그인 요청 정보 (이메일, 비밀번호)
     * @return JWT 토큰과 사용자 정보가 포함된 인증 응답
     * @throws RuntimeException 인증 실패 시
     */
    @Override
    public JwtAuthenticationResponse authenticate(UserLoginRequest request) {
        // 1. 사용자 조회
        Account account = accountRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() -> new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다."));

        // 2. 비밀번호 검증
        if (!matchesPassword(request.getPassword(), account.getPassword())) {
            throw new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // 3. Access/Refresh 토큰 발급
        String accessToken = jwtTokenProvider.generateAccessToken(account.getId(), account.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(account.getId());

        // 4. Refresh Token DB 저장 (기존 토큰 삭제 후 새 토큰 저장)
        refreshTokenRepository.deleteByAccount(account);
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .account(account)
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plusDays(7)) // 7일 후 만료
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        // 5. 사용자 정보 DTO 변환
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
        userProfile.setPreferWorkstyle(account.getPreferWorkstyle());
        userProfile.setDislikeWorkstyle(account.getDislikeWorkstyle());

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
     * Access Token 생성
     * - 짧은 만료시간을 가진 인증 토큰 생성
     * - API 요청 시 인증에 사용
     * 
     * @param userId 사용자 ID
     * @param email  사용자 이메일
     * @return 생성된 Access Token
     */
    @Override
    public String generateAccessToken(Long userId, String email) {
        return jwtTokenProvider.generateAccessToken(userId, email);
    }

    /**
     * Refresh Token 생성 및 저장
     * - 긴 만료시간을 가진 토큰으로 Access Token 갱신에 사용
     * - MySQL DB에 영구 저장하여 다중 디바이스 지원
     * 
     * @param userId 사용자 ID
     * @return 생성된 Refresh Token
     */
    @Override
    public String generateRefreshToken(Long userId) {
        Account account = accountRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

        // 기존 토큰 삭제 후 새 토큰 저장
        refreshTokenRepository.deleteByAccount(account);
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .account(account)
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plusDays(7)) // 7일 후 만료
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

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
     * @throws RuntimeException Refresh Token 검증 실패 시
     */
    @Override
    public JwtAuthenticationResponse refreshToken(RefreshTokenRequest request) {
        // 1. DB에서 Refresh Token 조회 및 검증
        String refreshTokenValue = request.getRefreshToken();
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 리프레시 토큰입니다."));

        // 2. 토큰 만료 확인
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("만료된 리프레시 토큰입니다.");
        }

        // 3. JWT 토큰 자체 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshTokenValue)) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 4. 사용자 정보 조회
        Account account = refreshToken.getAccount();
        if (account == null || account.getDeletedAt() != null) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }

        // 5. 새 Access/Refresh 토큰 발급
        String newAccessToken = jwtTokenProvider.generateAccessToken(account.getId(), account.getEmail());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(account.getId());

        // 6. 기존 토큰 삭제 후 새 토큰 저장
        refreshTokenRepository.deleteByAccount(account);
        RefreshToken newRefreshTokenEntity = RefreshToken.builder()
                .account(account)
                .token(newRefreshToken)
                .expiresAt(LocalDateTime.now().plusDays(7)) // 7일 후 만료
                .build();
        refreshTokenRepository.save(newRefreshTokenEntity);

        // 7. 사용자 정보 DTO 변환
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
        userProfile.setPreferWorkstyle(account.getPreferWorkstyle());
        userProfile.setDislikeWorkstyle(account.getDislikeWorkstyle());

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
     * - UserController의 TODO 해결을 위한 핵심 메서드
     * 
     * @return 현재 로그인된 사용자 ID, 인증되지 않은 경우 null
     */
    @Override
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getId();
        }

        return null;
    }
}
