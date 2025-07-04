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

        // ==================== 파일 관련 보안 이벤트 로깅 ====================

        /**
         * 파일 업로드 성공 이벤트 로깅
         */
        public void logFileUploadSuccess(Long userId, String userEmail, Long postId, String fileName,
                        long fileSize, String ipAddress, String userAgent) {
                log.info(
                                "FILE_UPLOAD_SUCCESS | UserId: {} | Email: {} | PostId: {} | FileName: {} | FileSize: {} bytes | IP: {} | UserAgent: {} | Time: {}",
                                userId,
                                userEmail,
                                postId,
                                fileName,
                                fileSize,
                                ipAddress,
                                userAgent,
                                LocalDateTime.now().format(TIMESTAMP_FORMAT));
        }

        /**
         * 파일 업로드 실패 이벤트 로깅
         */
        public void logFileUploadFailure(Long userId, String userEmail, Long postId, String fileName,
                        String reason, String ipAddress, String userAgent) {
                log.warn(
                                "FILE_UPLOAD_FAILURE | UserId: {} | Email: {} | PostId: {} | FileName: {} | Reason: {} | IP: {} | UserAgent: {} | Time: {}",
                                userId,
                                userEmail,
                                postId,
                                fileName,
                                reason,
                                ipAddress,
                                userAgent,
                                LocalDateTime.now().format(TIMESTAMP_FORMAT));
        }

        /**
         * 파일 다운로드 성공 이벤트 로깅
         */
        public void logFileDownloadSuccess(Long userId, String userEmail, Long attachId, String fileName,
                        long fileSize, String ipAddress, String userAgent) {
                log.info(
                                "FILE_DOWNLOAD_SUCCESS | UserId: {} | Email: {} | AttachId: {} | FileName: {} | FileSize: {} bytes | IP: {} | UserAgent: {} | Time: {}",
                                userId,
                                userEmail,
                                attachId,
                                fileName,
                                fileSize,
                                ipAddress,
                                userAgent,
                                LocalDateTime.now().format(TIMESTAMP_FORMAT));
        }

        /**
         * 파일 다운로드 실패 이벤트 로깅
         */
        public void logFileDownloadFailure(Long userId, String userEmail, Long attachId, String fileName,
                        String reason, String ipAddress, String userAgent) {
                log.warn(
                                "FILE_DOWNLOAD_FAILURE | UserId: {} | Email: {} | AttachId: {} | FileName: {} | Reason: {} | IP: {} | UserAgent: {} | Time: {}",
                                userId,
                                userEmail,
                                attachId,
                                fileName,
                                reason,
                                ipAddress,
                                userAgent,
                                LocalDateTime.now().format(TIMESTAMP_FORMAT));
        }

        /**
         * 파일 삭제 이벤트 로깅
         */
        public void logFileDelete(Long userId, String userEmail, Long attachId, String fileName,
                        String ipAddress, String userAgent) {
                log.info(
                                "FILE_DELETE | UserId: {} | Email: {} | AttachId: {} | FileName: {} | IP: {} | UserAgent: {} | Time: {}",
                                userId,
                                userEmail,
                                attachId,
                                fileName,
                                ipAddress,
                                userAgent,
                                LocalDateTime.now().format(TIMESTAMP_FORMAT));
        }

        /**
         * 악성 파일 업로드 시도 로깅
         */
        public void logMaliciousFileUploadAttempt(Long userId, String userEmail, String fileName,
                        String detectedReason, String ipAddress, String userAgent) {
                log.error(
                                "MALICIOUS_FILE_UPLOAD_ATTEMPT | UserId: {} | Email: {} | FileName: {} | Reason: {} | IP: {} | UserAgent: {} | Time: {}",
                                userId,
                                userEmail,
                                fileName,
                                detectedReason,
                                ipAddress,
                                userAgent,
                                LocalDateTime.now().format(TIMESTAMP_FORMAT));
        }

        /**
         * 파일 시그니처 불일치 감지 로깅
         */
        public void logFileSignatureMismatch(Long userId, String userEmail, String fileName,
                        String expectedExtension, String ipAddress, String userAgent) {
                log.warn(
                                "FILE_SIGNATURE_MISMATCH | UserId: {} | Email: {} | FileName: {} | ExpectedExt: {} | IP: {} | UserAgent: {} | Time: {}",
                                userId,
                                userEmail,
                                fileName,
                                expectedExtension,
                                ipAddress,
                                userAgent,
                                LocalDateTime.now().format(TIMESTAMP_FORMAT));
        }

        /**
         * 파일 크기 제한 초과 로깅
         */
        public void logFileSizeExceeded(Long userId, String userEmail, String fileName,
                        long fileSize, long maxAllowed, String ipAddress, String userAgent) {
                log.warn(
                                "FILE_SIZE_EXCEEDED | UserId: {} | Email: {} | FileName: {} | FileSize: {} bytes | MaxAllowed: {} bytes | IP: {} | UserAgent: {} | Time: {}",
                                userId,
                                userEmail,
                                fileName,
                                fileSize,
                                maxAllowed,
                                ipAddress,
                                userAgent,
                                LocalDateTime.now().format(TIMESTAMP_FORMAT));
        }

        /**
         * 권한 없는 파일 접근 시도 로깅
         */
        public void logUnauthorizedFileAccess(Long userId, String userEmail, Long attachId, String fileName,
                        String operation, String ipAddress, String userAgent) {
                log.warn(
                                "UNAUTHORIZED_FILE_ACCESS | UserId: {} | Email: {} | AttachId: {} | FileName: {} | Operation: {} | IP: {} | UserAgent: {} | Time: {}",
                                userId,
                                userEmail,
                                attachId,
                                fileName,
                                operation,
                                ipAddress,
                                userAgent,
                                LocalDateTime.now().format(TIMESTAMP_FORMAT));
        }

        /**
         * 파일 경로 조작 시도 로깅 (패스 트래버설 공격)
         */
        public void logPathTraversalAttempt(Long userId, String userEmail, String suspiciousPath,
                        String ipAddress, String userAgent) {
                log.error(
                                "PATH_TRAVERSAL_ATTEMPT | UserId: {} | Email: {} | SuspiciousPath: {} | IP: {} | UserAgent: {} | Time: {}",
                                userId,
                                userEmail,
                                suspiciousPath,
                                ipAddress,
                                userAgent,
                                LocalDateTime.now().format(TIMESTAMP_FORMAT));
        }

        // ==================== 프로필 이미지 전용 보안 이벤트 로깅 ====================

        /**
         * 프로필 이미지 업로드 성공 이벤트 로깅
         */
        public void logProfileImageUploadSuccess(Long userId, String userEmail, String fileName,
                        long fileSize, String ipAddress, String userAgent) {
                log.info("PROFILE_IMAGE_UPLOAD_SUCCESS | UserId: {} | Email: {} | FileName: {} | FileSize: {} bytes | IP: {} | UserAgent: {} | Time: {}",
                                userId,
                                userEmail,
                                fileName,
                                fileSize,
                                ipAddress,
                                userAgent,
                                LocalDateTime.now().format(TIMESTAMP_FORMAT));
        }

        /**
         * 프로필 이미지 업로드 실패 이벤트 로깅
         */
        public void logProfileImageUploadFailure(Long userId, String userEmail, String fileName,
                        String reason, String ipAddress, String userAgent) {
                log.warn("PROFILE_IMAGE_UPLOAD_FAILURE | UserId: {} | Email: {} | FileName: {} | Reason: {} | IP: {} | UserAgent: {} | Time: {}",
                                userId,
                                userEmail,
                                fileName,
                                reason,
                                ipAddress,
                                userAgent,
                                LocalDateTime.now().format(TIMESTAMP_FORMAT));
        }

        /**
         * 프로필 이미지 악성 파일 업로드 시도 로깅
         */
        public void logProfileImageMaliciousAttempt(Long userId, String userEmail, String fileName,
                        String detectedReason, String ipAddress, String userAgent) {
                log.error("PROFILE_IMAGE_MALICIOUS_ATTEMPT | UserId: {} | Email: {} | FileName: {} | Reason: {} | IP: {} | UserAgent: {} | Time: {}",
                                userId,
                                userEmail,
                                fileName,
                                detectedReason,
                                ipAddress,
                                userAgent,
                                LocalDateTime.now().format(TIMESTAMP_FORMAT));
        }

        /**
         * 프로필 이미지 MIME 타입 불일치 로깅
         */
        public void logProfileImageMimeTypeMismatch(Long userId, String userEmail, String fileName,
                        String actualMimeType, String expectedMimeTypes,
                        String ipAddress, String userAgent) {
                log.warn("PROFILE_IMAGE_MIME_MISMATCH | UserId: {} | Email: {} | FileName: {} | ActualMime: {} | ExpectedMimes: {} | IP: {} | UserAgent: {} | Time: {}",
                                userId,
                                userEmail,
                                fileName,
                                actualMimeType,
                                expectedMimeTypes,
                                ipAddress,
                                userAgent,
                                LocalDateTime.now().format(TIMESTAMP_FORMAT));
        }

        /**
         * 프로필 이미지 시그니처 불일치 로깅
         */
        public void logProfileImageSignatureMismatch(Long userId, String userEmail, String fileName,
                        String expectedExtension, String ipAddress, String userAgent) {
                log.warn("PROFILE_IMAGE_SIGNATURE_MISMATCH | UserId: {} | Email: {} | FileName: {} | ExpectedExt: {} | IP: {} | UserAgent: {} | Time: {}",
                                userId,
                                userEmail,
                                fileName,
                                expectedExtension,
                                ipAddress,
                                userAgent,
                                LocalDateTime.now().format(TIMESTAMP_FORMAT));
        }

        /**
         * 프로필 이미지 크기 초과 로깅
         */
        public void logProfileImageSizeExceeded(Long userId, String userEmail, String fileName,
                        long fileSize, long maxAllowed, String ipAddress, String userAgent) {
                log.warn("PROFILE_IMAGE_SIZE_EXCEEDED | UserId: {} | Email: {} | FileName: {} | FileSize: {} bytes | MaxAllowed: {} bytes | IP: {} | UserAgent: {} | Time: {}",
                                userId,
                                userEmail,
                                fileName,
                                fileSize,
                                maxAllowed,
                                ipAddress,
                                userAgent,
                                LocalDateTime.now().format(TIMESTAMP_FORMAT));
        }

        /**
         * 프로필 이미지 삭제 이벤트 로깅
         */
        public void logProfileImageDelete(Long userId, String userEmail, String fileName,
                        String ipAddress, String userAgent) {
                log.info("PROFILE_IMAGE_DELETE | UserId: {} | Email: {} | FileName: {} | IP: {} | UserAgent: {} | Time: {}",
                                userId,
                                userEmail,
                                fileName,
                                ipAddress,
                                userAgent,
                                LocalDateTime.now().format(TIMESTAMP_FORMAT));
        }
}
