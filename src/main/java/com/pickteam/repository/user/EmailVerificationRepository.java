package com.pickteam.repository.user;

import com.pickteam.domain.user.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 이메일 인증 리포지토리
 * - 이메일 인증 코드 DB 저장 및 조회
 * - 만료된 인증 코드 정리 기능
 */
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    /**
     * 이메일과 인증 코드로 인증 정보 조회
     * - 아직 인증되지 않은 코드만 조회
     * 
     * @param email            조회할 이메일 주소
     * @param verificationCode 확인할 인증 코드
     * @return 해당하는 EmailVerification 엔티티 (Optional)
     */
    Optional<EmailVerification> findByEmailAndVerificationCodeAndIsVerifiedFalse(
            String email, String verificationCode);

    /**
     * 특정 이메일의 미인증 상태인 인증 코드 조회
     * - 최신 인증 코드 확인용
     * 
     * @param email 조회할 이메일 주소
     * @return 해당하는 EmailVerification 엔티티 (Optional)
     */
    Optional<EmailVerification> findByEmailAndIsVerifiedFalse(String email);

    /**
     * 만료된 인증 코드 삭제
     * - 배치 작업이나 정리 작업에서 사용
     * 
     * @param now 현재 시각
     * @return 삭제된 레코드 수
     */
    @Modifying
    @Query("DELETE FROM EmailVerification e WHERE e.expiresAt < :now")
    int deleteExpiredVerifications(@Param("now") LocalDateTime now);

    /**
     * 특정 이메일의 모든 인증 코드 삭제
     * - 새로운 인증 코드 발급 시 기존 코드 정리용
     * 
     * @param email 삭제할 이메일 주소
     */
    void deleteByEmail(String email);
}
