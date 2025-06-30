package com.pickteam.service.security;

import com.pickteam.domain.user.Account;
import com.pickteam.repository.user.AccountRepository;
import com.pickteam.repository.user.RefreshTokenRepository;
import com.pickteam.repository.user.EmailVerificationRepository;
import jakarta.persistence.EntityManager;
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
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final EntityManager entityManager;

    /** 유예기간 (일) - 환경변수에서 주입 */
    @Value("${app.account.grace-period-days}")
    private int gracePeriodDays;

    /**
     * 유예기간 만료된 계정 영구 삭제
     * - 매일 새벽 2시에 실행 (cron: 0 0 2 * * ?)
     * - 개인정보보호법 준수를 위한 자동 삭제
     * - 연관 데이터 정리 포함
     */
    @Scheduled(cron = "${app.account.cleanup-schedule}")
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
     * - GDPR 및 개인정보보호법 준수
     * 
     * @param account 삭제할 계정
     */
    private void cleanupRelatedData(Account account) {
        log.debug("연관 데이터 정리 시작: 계정 ID={}", account.getId());

        try {
            // 1. 인증 관련 데이터 삭제 (보안 우선)
            cleanupAuthenticationData(account);

            // 2. 사용자 생성 콘텐츠 처리 (비즈니스 로직에 따라)
            cleanupUserContent(account);

            // 3. 멤버십 및 관계 데이터 삭제
            cleanupMembershipData(account);

            // 4. 개인정보 관련 데이터 삭제
            cleanupPersonalData(account);

            log.info("연관 데이터 정리 완료: 계정 ID={}", account.getId());

        } catch (Exception e) {
            log.error("연관 데이터 정리 중 오류 발생: 계정 ID={}, 오류={}",
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
     * 사용자 생성 콘텐츠 처리
     * - 댓글, 게시글 등 완전 삭제 (익명화 없이)
     * - 데이터 완전 제거로 개인정보보호법 준수
     */
    private void cleanupUserContent(Account account) {
        try {
            Long accountId = account.getId();

            log.debug("사용자 콘텐츠 삭제 시작: 계정 ID={}", accountId);

            // 1. 댓글 완전 삭제
            int deletedComments = entityManager.createQuery(
                    "DELETE FROM Comment c WHERE c.account.id = :accountId")
                    .setParameter("accountId", accountId)
                    .executeUpdate();
            log.debug("댓글 삭제 완료: {}개", deletedComments);

            // 2. 게시글 완전 삭제 (첨부파일 포함)
            int deletedPosts = entityManager.createQuery(
                    "DELETE FROM Post p WHERE p.account.id = :accountId")
                    .setParameter("accountId", accountId)
                    .executeUpdate();
            log.debug("게시글 삭제 완료: {}개", deletedPosts);

            // 3. 채팅 메시지 완전 삭제
            int deletedChatMessages = entityManager.createQuery(
                    "DELETE FROM ChatMessage cm WHERE cm.account.id = :accountId")
                    .setParameter("accountId", accountId)
                    .executeUpdate();
            log.debug("채팅 메시지 삭제 완료: {}개", deletedChatMessages);

            // 4. 칸반 태스크 댓글 삭제
            int deletedKanbanComments = entityManager.createQuery(
                    "DELETE FROM KanbanTaskComment ktc WHERE ktc.account.id = :accountId")
                    .setParameter("accountId", accountId)
                    .executeUpdate();
            log.debug("칸반 태스크 댓글 삭제 완료: {}개", deletedKanbanComments);

            log.info("사용자 콘텐츠 삭제 완료: 댓글={}, 게시글={}, 채팅={}, 칸반댓글={}",
                    deletedComments, deletedPosts, deletedChatMessages, deletedKanbanComments);

        } catch (Exception e) {
            log.error("사용자 콘텐츠 정리 실패: 계정 ID={}", account.getId(), e);
            throw e; // 트랜잭션 롤백을 위해 예외 재발생
        }
    }

    /**
     * 멤버십 및 관계 데이터 삭제
     * - 워크스페이스, 팀, 화상회의 멤버십 등 명시적 삭제
     * - FK 제약 조건 오류 방지를 위한 확실한 삭제
     */
    private void cleanupMembershipData(Account account) {
        try {
            Long accountId = account.getId();

            log.debug("멤버십 데이터 삭제 시작: 계정 ID={}", accountId);

            // 1. 워크스페이스 멤버십 삭제
            int deletedWorkspaceMembers = entityManager.createQuery(
                    "DELETE FROM WorkspaceMember wm WHERE wm.account.id = :accountId")
                    .setParameter("accountId", accountId)
                    .executeUpdate();
            log.debug("워크스페이스 멤버십 삭제 완료: {}개", deletedWorkspaceMembers);

            // 2. 팀 멤버십 삭제
            int deletedTeamMembers = entityManager.createQuery(
                    "DELETE FROM TeamMember tm WHERE tm.account.id = :accountId")
                    .setParameter("accountId", accountId)
                    .executeUpdate();
            log.debug("팀 멤버십 삭제 완료: {}개", deletedTeamMembers);

            // 3. 화상회의 멤버십 삭제
            int deletedVideoMembers = entityManager.createQuery(
                    "DELETE FROM VideoMember vm WHERE vm.account.id = :accountId")
                    .setParameter("accountId", accountId)
                    .executeUpdate();
            log.debug("화상회의 멤버십 삭제 완료: {}개", deletedVideoMembers);

            // 4. 칸반 태스크 멤버십 삭제
            int deletedKanbanMembers = entityManager.createQuery(
                    "DELETE FROM KanbanTaskMember ktm WHERE ktm.account.id = :accountId")
                    .setParameter("accountId", accountId)
                    .executeUpdate();
            log.debug("칸반 태스크 멤버십 삭제 완료: {}개", deletedKanbanMembers);

            // 5. 채팅방 멤버십 삭제
            int deletedChatMembers = entityManager.createQuery(
                    "DELETE FROM ChatMember cm WHERE cm.account.id = :accountId")
                    .setParameter("accountId", accountId)
                    .executeUpdate();
            log.debug("채팅방 멤버십 삭제 완료: {}개", deletedChatMembers);

            log.info("멤버십 데이터 삭제 완료: 워크스페이스={}, 팀={}, 화상회의={}, 칸반={}, 채팅={}",
                    deletedWorkspaceMembers, deletedTeamMembers, deletedVideoMembers,
                    deletedKanbanMembers, deletedChatMembers);

        } catch (Exception e) {
            log.error("멤버십 데이터 정리 실패: 계정 ID={}", account.getId(), e);
            throw e; // 트랜잭션 롤백을 위해 예외 재발생
        }
    }

    /**
     * 개인정보 관련 데이터 삭제
     * - 알림 로그, 해시태그, 스케줄 등 GDPR 준수를 위한 완전 삭제
     */
    private void cleanupPersonalData(Account account) {
        try {
            Long accountId = account.getId();

            log.debug("개인정보 데이터 삭제 시작: 계정 ID={}", accountId);

            // 1. 알림 로그 삭제 (개인정보 포함)
            int deletedNotificationLogs = entityManager.createQuery(
                    "DELETE FROM NotificationLog nl WHERE nl.account.id = :accountId")
                    .setParameter("accountId", accountId)
                    .executeUpdate();
            log.debug("알림 로그 삭제 완료: {}개", deletedNotificationLogs);

            // 2. 사용자 해시태그 연결 삭제
            int deletedHashtagLists = entityManager.createQuery(
                    "DELETE FROM UserHashtagList uhl WHERE uhl.account.id = :accountId")
                    .setParameter("accountId", accountId)
                    .executeUpdate();
            log.debug("사용자 해시태그 삭제 완료: {}개", deletedHashtagLists);

            // 3. 개인 스케줄 삭제 (개인정보 포함 가능)
            int deletedSchedules = entityManager.createQuery(
                    "DELETE FROM Schedule s WHERE s.account.id = :accountId")
                    .setParameter("accountId", accountId)
                    .executeUpdate();
            log.debug("개인 스케줄 삭제 완료: {}개", deletedSchedules);

            // 4. 기타 개인 설정 데이터 삭제 (필요시 추가)
            // 예: 사용자 설정, 즐겨찾기, 최근 활동 기록 등

            log.info("개인정보 데이터 삭제 완료: 알림={}, 해시태그={}, 스케줄={}",
                    deletedNotificationLogs, deletedHashtagLists, deletedSchedules);

        } catch (Exception e) {
            log.error("개인정보 데이터 정리 실패: 계정 ID={}", account.getId(), e);
            throw e; // 트랜잭션 롤백을 위해 예외 재발생
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
