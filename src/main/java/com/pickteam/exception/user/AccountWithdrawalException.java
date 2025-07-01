package com.pickteam.exception.user;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 탈퇴 계정 관련 예외
 * - 탈퇴 유예 기간 중인 계정으로 시도한 작업에 대한 예외
 * - 유예 기간 정보와 함께 사용자 친화적인 메시지 제공
 */
public class AccountWithdrawalException extends RuntimeException {

    private final LocalDateTime permanentDeletionDate;
    private final long remainingDays;

    /**
     * 탈퇴 계정 예외 생성자
     * 
     * @param message               기본 메시지
     * @param permanentDeletionDate 완전 삭제 예정일
     */
    public AccountWithdrawalException(String message, LocalDateTime permanentDeletionDate) {
        super(message);
        this.permanentDeletionDate = permanentDeletionDate;
        this.remainingDays = calculateRemainingDays(permanentDeletionDate);
    }

    /**
     * 메시지만으로 생성하는 생성자
     * 
     * @param message 예외 메시지
     */
    public AccountWithdrawalException(String message) {
        super(message);
        this.permanentDeletionDate = null;
        this.remainingDays = -1;
    }

    /**
     * 남은 일수를 계산하는 메서드
     * - 음수 방지 (이미 지난 날짜의 경우 0 반환)
     * - 계산 로직 재사용 가능
     * 
     * @param permanentDeletionDate 완전 삭제 예정일
     * @return 남은 일수 (최소 0)
     */
    private static long calculateRemainingDays(LocalDateTime permanentDeletionDate) {
        if (permanentDeletionDate == null) {
            return -1;
        }
        return Math.max(0, ChronoUnit.DAYS.between(LocalDateTime.now(), permanentDeletionDate));
    }

    public LocalDateTime getPermanentDeletionDate() {
        return permanentDeletionDate;
    }

    public long getRemainingDays() {
        return remainingDays;
    }

    /**
     * 유예 기간 정보가 포함된 상세 메시지 반환
     */
    public String getDetailedMessage() {
        if (permanentDeletionDate != null) {
            return String.format("해당 이메일은 탈퇴 처리 중입니다. 완전 삭제 예정일: %s (남은 일수: %d일)",
                    permanentDeletionDate.toLocalDate(), remainingDays);
        }
        return getMessage();
    }
}
