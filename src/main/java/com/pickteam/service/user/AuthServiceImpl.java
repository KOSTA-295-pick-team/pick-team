package com.pickteam.service.user;

import com.pickteam.dto.user.UserLoginRequest;
import com.pickteam.dto.security.JwtAuthenticationResponse;
import com.pickteam.dto.security.RefreshTokenRequest;
import com.pickteam.repository.user.AccountRepository;
import com.pickteam.domain.user.Account;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final com.pickteam.security.JwtTokenProvider jwtTokenProvider;
    private final com.pickteam.security.CustomUserDetailsService userDetailsService;
    private final com.pickteam.repository.user.RefreshTokenRepository refreshTokenRepository;

    // refreshToken 임시 저장 (실서비스는 Redis/DB 권장)
    private final java.util.Map<Long, String> refreshTokenStore = new java.util.concurrent.ConcurrentHashMap<>();

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
        refreshTokenStore.put(account.getId(), refreshToken);

        // 4. 사용자 정보 DTO 변환
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

        // 5. 응답 반환
        return new JwtAuthenticationResponse(
                accessToken,
                refreshToken,
                jwtTokenProvider.getJwtExpirationMs(),
                userProfile);
    }

    @Override
    public String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public boolean matchesPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public String generateAccessToken(Long userId, String email) {
        return jwtTokenProvider.generateAccessToken(userId, email);
    }

    @Override
    public String generateRefreshToken(Long userId) {
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);
        refreshTokenStore.put(userId, refreshToken);
        return refreshToken;
    }

    @Override
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    @Override
    public JwtAuthenticationResponse refreshToken(RefreshTokenRequest request) {
        // 1. 리프레시 토큰에서 userId 추출
        String refreshToken = request.getRefreshToken();
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("유효하지 않은 리프레시 토큰입니다.");
        }
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String storedToken = refreshTokenStore.get(userId);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new RuntimeException("리프레시 토큰이 일치하지 않습니다.");
        }
        // 2. 사용자 정보 조회
        Account account = accountRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 3. 새 Access/Refresh 토큰 발급
        String newAccessToken = jwtTokenProvider.generateAccessToken(account.getId(), account.getEmail());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(account.getId());
        refreshTokenStore.put(account.getId(), newRefreshToken);
        // 4. 사용자 정보 DTO 변환
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
        // 5. 응답 반환
        return new JwtAuthenticationResponse(
                newAccessToken,
                newRefreshToken,
                jwtTokenProvider.getJwtExpirationMs(),
                userProfile);
    }

    @Override
    public Long getCurrentUserId() {
        // TODO: Spring Security Context에서 현재 사용자 ID 가져오기
        return null;
    }
}
