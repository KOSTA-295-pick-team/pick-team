package com.pickteam.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.pickteam.domain.user.Account;
import com.pickteam.domain.enums.UserRole;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {

        // ID 중복검사 (활성 계정만)
        boolean existsByEmailAndDeletedAtIsNull(String email);

        // ID 중복검사 (삭제된 계정 포함 - 필요시 사용)
        boolean existsByEmail(String email);

        // 이메일로 사용자 찾기
        Optional<Account> findByEmail(String email);

        // 이메일로 활성 사용자 찾기 (Security용)
        Optional<Account> findByEmailAndDeletedAtIsNull(String email);

        // 이메일과 비밀번호로 사용자 찾기 (로그인용)
        Optional<Account> findByEmailAndPassword(String email, String password);

        // 삭제되지 않은 사용자만 조회
        Optional<Account> findByIdAndDeletedAtIsNull(Long id);

        // 전체 활성 사용자 조회
        java.util.List<Account> findAllByDeletedAtIsNull();

        // 추천 팀원을 위한 MBTI 기반 조회
        List<Account> findByMbtiAndDeletedAtIsNullAndIdNot(String mbti, Long excludeId);

        // 추천 팀원을 위한 성향 기반 조회
        List<Account> findByDispositionContainingAndDeletedAtIsNullAndIdNot(String disposition, Long excludeId);

        // 특정 역할의 사용자 조회
        List<Account> findByRoleAndDeletedAtIsNull(UserRole role);

        // 이름으로 사용자 검색 (부분 일치)
        List<Account> findByNameContainingIgnoreCaseAndDeletedAtIsNull(String name);

        // 복합 조건으로 추천 팀원 조회 (MBTI + 성향)
        @Query("SELECT a FROM Account a WHERE a.deletedAt IS NULL AND a.id != :excludeId " +
                        "AND (a.mbti = :mbti OR a.disposition LIKE %:disposition%) " +
                        "ORDER BY CASE WHEN a.mbti = :mbti THEN 1 ELSE 2 END")
        List<Account> findRecommendedTeamMembers(@Param("mbti") String mbti,
                        @Param("disposition") String disposition,
                        @Param("excludeId") Long excludeId);

        // === 계정 삭제 관련 쿼리 ===

        // 유예기간이 만료된 계정 조회 (영구 삭제 대상)
        @Query("SELECT a FROM Account a WHERE a.permanentDeletionDate IS NOT NULL " +
                        "AND a.permanentDeletionDate < CURRENT_TIMESTAMP")
        List<Account> findAccountsToHardDelete();

        // 유예기간이 만료된 계정 개수 조회
        @Query("SELECT COUNT(a) FROM Account a WHERE a.permanentDeletionDate IS NOT NULL " +
                        "AND a.permanentDeletionDate < CURRENT_TIMESTAMP")
        long countAccountsToHardDelete();

        // 특정 기간 이전에 삭제된 계정 조회 (배치 처리용)
        @Query("SELECT a FROM Account a WHERE a.permanentDeletionDate IS NOT NULL " +
                        "AND a.permanentDeletionDate < :cutoffDate")
        List<Account> findAccountsToHardDeleteBefore(@Param("cutoffDate") LocalDateTime cutoffDate);

        // 영구 삭제 예정인 계정 조회 (복구 가능한 계정)
        @Query("SELECT a FROM Account a WHERE a.permanentDeletionDate IS NOT NULL " +
                        "AND a.permanentDeletionDate > CURRENT_TIMESTAMP " +
                        "AND a.deletedAt IS NOT NULL")
        List<Account> findAccountsInGracePeriod();

        // 탈퇴 유예 기간 중인 특정 이메일 계정 조회
        @Query("SELECT a FROM Account a WHERE a.email = :email " +
                        "AND a.permanentDeletionDate IS NOT NULL " +
                        "AND a.permanentDeletionDate > CURRENT_TIMESTAMP " +
                        "AND a.deletedAt IS NOT NULL")
        Optional<Account> findWithdrawnAccountByEmail(@Param("email") String email);

        // 특정 이메일의 탈퇴 상태 확인
        @Query("SELECT COUNT(a) > 0 FROM Account a WHERE a.email = :email " +
                        "AND a.permanentDeletionDate IS NOT NULL " +
                        "AND a.permanentDeletionDate > CURRENT_TIMESTAMP " +
                        "AND a.deletedAt IS NOT NULL")
        boolean existsWithdrawnAccountByEmail(@Param("email") String email);

        // === isDeleted 기반 조회 메서드 (수동 Soft Delete 지원) ===

        // 활성 계정 조회 (isDeleted = false)
        Optional<Account> findByIdAndIsDeletedFalse(Long id);

        // 활성 계정 전체 조회
        List<Account> findAllByIsDeletedFalse();

        // 이메일로 활성 계정 조회
        Optional<Account> findByEmailAndIsDeletedFalse(String email);

        // 이메일 중복 체크 (활성 계정만)
        boolean existsByEmailAndIsDeletedFalse(String email);

        // 사용자명 중복 체크 (활성 계정만)
        boolean existsByNameAndDeletedAtIsNull(String name);

        // OAuth 관련 조회 메서드

        /** OAuth 제공자와 제공자 ID로 계정 조회 */
        Optional<Account> findByProviderAndProviderId(com.pickteam.domain.enums.AuthProvider provider,
                        String providerId);

        /** OAuth 제공자와 제공자 ID로 활성 계정 조회 */
        Optional<Account> findByProviderAndProviderIdAndDeletedAtIsNull(com.pickteam.domain.enums.AuthProvider provider,
                        String providerId);

        /** 특정 OAuth 제공자로 가입한 활성 사용자 목록 조회 */
        List<Account> findByProviderAndDeletedAtIsNull(com.pickteam.domain.enums.AuthProvider provider);

}
