package com.pickteam.service.security;

import com.pickteam.domain.user.Account;
import com.pickteam.repository.user.AccountRepository;
import com.pickteam.repository.user.RefreshTokenRepository;
import com.pickteam.repository.user.EmailVerificationRepository;
import com.pickteam.repository.user.UserHashtagListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 계정 개인정보 삭제 스케줄러
 * - 유예기간이 만료된 계정의 개인정보를 삭제 (계정은 유지)
 * - 개인정보보호법 준수를 위한 자동 데이터 삭제
 * - 스케줄링을 통한 주기적 정리 작업 수행
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class AccountCleanupScheduler {

    private final AccountRepository accountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final UserHashtagListRepository userHashtagListRepository;

    /** 유예기간 (일) - 환경변수에서 주입 */
    @Value("${app.account.grace-period-days}")
    private int gracePeriodDays;

    /**
     * 유예기간 만료된 계정 개인정보 삭제
     * - 매일 새벽 2시에 실행 (cron: 0 0 2 * * ?)
     * - 개인정보보호법 준수를 위한 개인정보 삭제
     * - 계정은 유지하되 개인식별정보만 제거
     */
    @Scheduled(cron = "${app.account.cleanup-schedule}")
    @Transactional
    public void processExpiredAccounts() {
        log.info("===== 계정 개인정보 삭제 스케줄러 시작 =====");

        try {
            // 개인정보 삭제 대상 계정 조회
            List<Account> accountsToProcess = accountRepository.findAccountsToHardDelete();

            if (accountsToProcess.isEmpty()) {
                log.info("개인정보 삭제할 계정이 없습니다.");
                return;
            }

            log.info("개인정보 삭제 대상 계정 수: {}개", accountsToProcess.size());

            int processedCount = 0;
            for (Account account : accountsToProcess) {
                try {
                    // 개인정보 삭제 전 필요한 연관 데이터 정리
                    cleanupSensitiveData(account);

                    // 개인정보 삭제 (계정은 유지)
                    account.removePersonalInformation();
                    accountRepository.save(account);
                    processedCount++;

                    log.info("계정 개인정보 삭제 완료: ID={}, 기존이메일={}, 삭제예정일={}",
                            account.getId(),
                            maskEmail(account.getEmail()), // 이미 null이 될 수 있음
                            account.getPermanentDeletionDate());

                } catch (Exception e) {
                    log.error("계정 개인정보 삭제 실패: ID={}, 오류={}",
                            account.getId(), e.getMessage(), e);
                }
            }

            log.info("===== 계정 개인정보 삭제 스케줄러 완료: {}개 처리 =====", processedCount);

        } catch (Exception e) {
            log.error("계정 개인정보 삭제 스케줄러 실행 중 오류 발생", e);
        }
    }

    /**
     * 개인정보 삭제 전 민감한 데이터 정리
     * - 인증 관련 데이터 삭제 (토큰, 인증 코드)
     * - 개인정보 포함된 알림 등 정리
     * - 계정 자체와 연관 데이터(게시글, 댓글 등)는 보존
     * 
     * @param account 처리할 계정
     */
    private void cleanupSensitiveData(Account account) {
        log.debug("민감한 데이터 정리 시작: 계정 ID={}", account.getId());

        try {
            // 1. 인증 관련 데이터 삭제 (보안 우선)
            cleanupAuthenticationData(account);

            // 2. User 도메인 관련 데이터 삭제 (JPA Repository 사용)
            cleanupUserRelatedData(account);

            log.info("민감한 데이터 정리 완료: 계정 ID={}", account.getId());

        } catch (Exception e) {
            log.error("민감한 데이터 정리 중 오류 발생: 계정 ID={}, 오류={}",
                    account.getId(), e.getMessage(), e);
            throw e; // 트랜잭션 롤백을 위해 예외 재발생
        }
    }

    /**
     * 인증 관련 데이터 삭제
     * - RefreshToken, EmailVerification 등 보안 데이터
     */
    private void cleanupAuthenticationData(Account account) {
        try {
            // RefreshToken 삭제
            refreshTokenRepository.deleteByAccount(account);
            log.debug("RefreshToken 삭제 완료");

            // 이메일 인증 데이터 삭제
            emailVerificationRepository.deleteByEmail(account.getEmail());
            log.debug("이메일 인증 데이터 삭제 완료");

        } catch (Exception e) {
            log.error("인증 데이터 정리 실패: 계정 ID={}", account.getId(), e);
        }
    }

    /**
     * 이메일 마스킹 (개인정보 보호)
     * - 로그에 이메일 출력 시 개인정보 보호를 위해 마스킹
     * 
     * @param email 원본 이메일
     * @return 마스킹된 이메일
     */
    private String maskEmail(String email) {
        if (email == null || email.length() < 3) {
            return "***";
        }

        int atIndex = email.indexOf('@');
        if (atIndex == -1) {
            return email.substring(0, 2) + "***";
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
     * 현재 유예기간 만료 예정 계정 수 조회
     * - 모니터링 및 알림 용도
     * 
     * @return 영구 삭제 예정 계정 수
     */
    public long getAccountsScheduledForDeletionCount() {
        return accountRepository.countAccountsToHardDelete();
    }

    /**
     * 유예기간 내 복구 가능한 계정 목록 조회
     * - 관리자용 복구 기능
     * 
     * @return 복구 가능한 계정 목록
     */
    public List<Account> getAccountsInGracePeriod() {
        return accountRepository.findAccountsInGracePeriod();
    }

    /**
     * 수동으로 특정 기간 이전 계정 개인정보 삭제
     * - 관리자 전용 기능
     * - 비상시 또는 대량 처리가 필요한 경우
     * 
     * @param cutoffDate 기준 날짜 (이 날짜 이전에 삭제 예정인 계정들 처리)
     * @return 처리된 계정 수
     */
    @Transactional
    public int manualProcessAccountsBefore(LocalDateTime cutoffDate) {
        log.warn("수동 계정 개인정보 삭제 시작: 기준일={}", cutoffDate);

        List<Account> accountsToProcess = accountRepository.findAccountsToHardDeleteBefore(cutoffDate);

        int processedCount = 0;
        for (Account account : accountsToProcess) {
            try {
                cleanupSensitiveData(account);
                account.removePersonalInformation();
                accountRepository.save(account);
                processedCount++;
            } catch (Exception e) {
                log.error("수동 처리 중 오류: 계정 ID={}", account.getId(), e);
            }
        }

        log.warn("수동 계정 개인정보 삭제 완료: {}개 처리", processedCount);
        return processedCount;
    }

    /**
     * User 도메인 관련 데이터 삭제 (JPA Repository 사용)
     * - 사용자 해시태그 연결 정보 등 User 도메인 데이터
     * - JPA Repository 메서드를 활용한 안전한 삭제
     * 
     * @param account 처리할 계정
     */
    private void cleanupUserRelatedData(Account account) {
        try {
            log.debug("User 도메인 데이터 삭제 시작: 계정 ID={}", account.getId());

            // 사용자 해시태그 연결 삭제 (JPA Repository 사용)
            userHashtagListRepository.deleteByAccount(account);
            log.debug("사용자 해시태그 연결 삭제 완료");

            log.info("User 도메인 데이터 정리 완료: 계정 ID={}", account.getId());

        } catch (Exception e) {
            log.error("User 도메인 데이터 정리 실패: 계정 ID={}", account.getId(), e);
            throw e; // 트랜잭션 롤백을 위해 예외 재발생
        }
    }
}
