package com.pickteam.service.security;

import com.pickteam.domain.user.Account;
import com.pickteam.domain.user.RefreshToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 보안 관련 이벤트 로깅 서비스
 * - 로그인, 로그아웃, 세션 관련 보안 이벤트 로깅
 * - 보안 감사를 위한 상세 로그 기록
 */
@Service
@Slf4j
public class SecurityAuditLogger {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 로그인 성공 이벤트 로깅
     */
    public void logLoginSuccess(Account account, RefreshToken refreshToken) {
        log.info("LOGIN_SUCCESS | UserId: {} | Email: {} | IP: {} | Device: {} | Time: {}",
                account.getId(),
                account.getEmail(),
                refreshToken.getIpAddress(),
                refreshToken.getDeviceInfo(),
                refreshToken.getLoginTime().format(TIMESTAMP_FORMAT));
    }

    /**
     * 로그인 실패 이벤트 로깅
     */
    public void logLoginFailure(String email, String ipAddress, String reason) {
        log.warn("LOGIN_FAILURE | Email: {} | IP: {} | Reason: {} | Time: {}",
                email,
                ipAddress,
                reason,
                LocalDateTime.now().format(TIMESTAMP_FORMAT));
    }

    /**
     * 로그아웃 이벤트 로깅
     */
    public void logLogout(Account account, String ipAddress, int invalidatedSessions) {
        log.info("LOGOUT | UserId: {} | Email: {} | IP: {} | InvalidatedSessions: {} | Time: {}",
                account.getId(),
                account.getEmail(),
                ipAddress,
                invalidatedSessions,
                LocalDateTime.now().format(TIMESTAMP_FORMAT));
    }

    /**
     * 세션 무효화 이벤트 로깅
     */
    public void logSessionInvalidation(Account account, RefreshToken refreshToken, String reason) {
        log.info("SESSION_INVALIDATED | UserId: {} | Email: {} | IP: {} | Device: {} | Reason: {} | Time: {}",
                account.getId(),
                account.getEmail(),
                refreshToken.getIpAddress(),
                refreshToken.getDeviceInfo(),
                reason,
                LocalDateTime.now().format(TIMESTAMP_FORMAT));
    }

    /**
     * 중복 로그인 감지 이벤트 로깅
     */
    public void logDuplicateLogin(Account account, String newIpAddress, String newDevice, int previousSessions) {
        log.warn(
                "DUPLICATE_LOGIN_DETECTED | UserId: {} | Email: {} | NewIP: {} | NewDevice: {} | PreviousSessions: {} | Time: {}",
                account.getId(),
                account.getEmail(),
                newIpAddress,
                newDevice,
                previousSessions,
                LocalDateTime.now().format(TIMESTAMP_FORMAT));
    }

    /**
     * 만료된 토큰 접근 시도 로깅
     */
    public void logExpiredTokenAccess(String email, String ipAddress, String userAgent) {
        log.warn("EXPIRED_TOKEN_ACCESS | Email: {} | IP: {} | UserAgent: {} | Time: {}",
                email,
                ipAddress,
                userAgent,
                LocalDateTime.now().format(TIMESTAMP_FORMAT));
    }

    /**
     * 무효한 토큰 접근 시도 로깅
     */
    public void logInvalidTokenAccess(String ipAddress, String userAgent, String reason) {
        log.warn("INVALID_TOKEN_ACCESS | IP: {} | UserAgent: {} | Reason: {} | Time: {}",
                ipAddress,
                userAgent,
                reason,
                LocalDateTime.now().format(TIMESTAMP_FORMAT));
    }

    /**
     * 토큰 갱신 이벤트 로깅
     */
    public void logTokenRefresh(Account account, String ipAddress) {
        log.info("TOKEN_REFRESH | UserId: {} | Email: {} | IP: {} | Time: {}",
                account.getId(),
                account.getEmail(),
                ipAddress,
                LocalDateTime.now().format(TIMESTAMP_FORMAT));
    }

    /**
     * 의심스러운 활동 로깅
     */
    public void logSuspiciousActivity(String activityType, String details, String ipAddress) {
        log.warn("SUSPICIOUS_ACTIVITY | Type: {} | Details: {} | IP: {} | Time: {}",
                activityType,
                details,
                ipAddress,
                LocalDateTime.now().format(TIMESTAMP_FORMAT));
    }
}
