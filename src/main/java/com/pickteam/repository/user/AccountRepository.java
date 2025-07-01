package com.pickteam.repository.user;

import com.pickteam.domain.enums.UserRole;
import com.pickteam.domain.user.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
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
}