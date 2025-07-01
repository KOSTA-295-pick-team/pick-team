package com.pickteam.service.security;

import com.pickteam.domain.user.Account;
import com.pickteam.repository.user.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 계정 정리 스케줄러
 * - 유예기간이 만료된 계정을 영구 삭제 (hard-delete)
 * - 개인정보보호법 준수를 위한 자동 데이터 삭제
 * - 스케줄링을 통한 주기적 정리 작업 수행
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountCleanupScheduler {

    private final AccountRepository accountRepository;

    /** 유예기간 (일) - 기본값: 30일 */
    @Value("${app.account.grace-period-days:30}")
    private int gracePeriodDays;

    /**
     * 유예기간 만료된 계정 영구 삭제
     * - 매일 새벽 2시에 실행 (cron: 0 0 2 * * ?)
     * - 개인정보보호법 준수를 위한 자동 삭제
     * - 연관 데이터 정리 포함
     */
    @Scheduled(cron = "${app.account.cleanup-schedule:0 0 2 * * ?}")
    @Transactional
    public void cleanupExpiredAccounts() {
        log.info("===== 계정 정리 스케줄러 시작 =====");

        try {
            // 영구 삭제 대상 계정 조회
            List<Account> accountsToDelete = accountRepository.findAccountsToHardDelete();

            if (accountsToDelete.isEmpty()) {
                log.info("영구 삭제할 계정이 없습니다.");
                return;
            }

            log.info("영구 삭제 대상 계정 수: {}개", accountsToDelete.size());

            int deletedCount = 0;
            for (Account account : accountsToDelete) {
                try {
                    // 연관 데이터 정리 (필요시)
                    cleanupRelatedData(account);

                    // 영구 삭제 (hard-delete)
                    accountRepository.delete(account);
                    deletedCount++;

                    log.info("계정 영구 삭제 완료: ID={}, 이메일={}, 삭제예정일={}",
                            account.getId(),
                            maskEmail(account.getEmail()),
                            account.getPermanentDeletionDate());

                } catch (Exception e) {
                    log.error("계정 영구 삭제 실패: ID={}, 이메일={}, 오류={}",
                            account.getId(),
                            maskEmail(account.getEmail()),
                            e.getMessage(), e);
                }
            }

            log.info("===== 계정 정리 스케줄러 완료: {}개 삭제 =====", deletedCount);

        } catch (Exception e) {
            log.error("계정 정리 스케줄러 실행 중 오류 발생", e);
        }
    }

    /**
     * 연관 데이터 정리
     * - 사용자와 연관된 모든 데이터를 안전하게 처리
     * - 비즈니스 로직에 따라 삭제 또는 익명화 수행
     * 
     * @param account 삭제할 계정
     */
    private void cleanupRelatedData(Account account) {
        log.debug("연관 데이터 정리 시작: 계정 ID={}", account.getId());

        // TODO: 실제 연관 데이터 정리 로직 구현
        // 예시:
        // 1. 댓글: 작성자 정보 익명화 또는 삭제
        // 2. 채팅 메시지: 발송자 정보 익명화
        // 3. 워크스페이스 멤버십: 삭제
        // 4. 팀 멤버십: 삭제
        // 5. 알림 로그: 삭제
        // 6. 해시태그 연결: 삭제

        // 현재는 로그만 남김 (실제 구현시 각 서비스별 정리 로직 호출)
        log.debug("연관 데이터 정리 필요: 댓글={}, 채팅메시지={}, 워크스페이스멤버={}, 팀멤버={}",
                account.getComments().size(),
                account.getChatMessages().size(),
                account.getWorkspaceMembers().size(),
                account.getTeamMembers().size());
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
     * 수동으로 특정 기간 이전 계정 정리
     * - 관리자 전용 기능
     * - 비상시 또는 대량 정리가 필요한 경우
     * 
     * @param cutoffDate 기준 날짜 (이 날짜 이전에 삭제 예정인 계정들 삭제)
     * @return 삭제된 계정 수
     */
    @Transactional
    public int manualCleanupAccountsBefore(LocalDateTime cutoffDate) {
        log.warn("수동 계정 정리 시작: 기준일={}", cutoffDate);

        List<Account> accountsToDelete = accountRepository.findAccountsToHardDeleteBefore(cutoffDate);

        for (Account account : accountsToDelete) {
            cleanupRelatedData(account);
            accountRepository.delete(account);
        }

        log.warn("수동 계정 정리 완료: {}개 삭제", accountsToDelete.size());
        return accountsToDelete.size();
    }
}
