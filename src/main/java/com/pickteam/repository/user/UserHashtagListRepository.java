package com.pickteam.repository.user;

import com.pickteam.domain.user.Account;
import com.pickteam.domain.user.UserHashtag;
import com.pickteam.domain.user.UserHashtagList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserHashtagListRepository extends JpaRepository<UserHashtagList, Long> {

    // 사용자의 해시태그 목록 조회 (User ID로)
    @Query("SELECT uhl FROM UserHashtagList uhl JOIN FETCH uhl.userHashtag WHERE uhl.account.id = :userId")
    List<UserHashtagList> findByAccountId(@Param("userId") Long userId);

    // 사용자의 해시태그 목록 조회 (Account 객체로)
    @Query("SELECT uhl FROM UserHashtagList uhl JOIN FETCH uhl.userHashtag WHERE uhl.account = :account")
    List<UserHashtagList> findByAccount(@Param("account") Account account);

    // 사용자와 해시태그로 찾기
    Optional<UserHashtagList> findByAccountAndUserHashtag(Account account, UserHashtag userHashtag);

    // 사용자가 특정 해시태그를 가지고 있는지 확인
    boolean existsByAccountAndUserHashtag(Account account, UserHashtag userHashtag);

    // 특정 사용자의 모든 해시태그 연결 삭제 (계정 삭제 시 사용)
    void deleteByAccount(Account account);

    // 특정 사용자 ID의 모든 해시태그 연결 삭제 (계정 삭제 시 사용)
    void deleteByAccountId(Long accountId);

    // === isDeleted 기반 조회 메서드 (수동 Soft Delete 지원) ===

    // 활성 해시태그 연결만 조회 (Account ID로)
    @Query("SELECT uhl FROM UserHashtagList uhl JOIN FETCH uhl.userHashtag WHERE uhl.account.id = :userId AND uhl.isDeleted = false")
    List<UserHashtagList> findByAccountIdAndIsDeletedFalse(@Param("userId") Long userId);

    // 활성 해시태그 연결만 조회 (Account 객체로)
    @Query("SELECT uhl FROM UserHashtagList uhl JOIN FETCH uhl.userHashtag WHERE uhl.account = :account AND uhl.isDeleted = false")
    List<UserHashtagList> findByAccountAndIsDeletedFalse(@Param("account") Account account);

    // 활성 연결 중에서 사용자와 해시태그로 찾기
    Optional<UserHashtagList> findByAccountAndUserHashtagAndIsDeletedFalse(Account account, UserHashtag userHashtag);

    // 활성 연결 중에서 사용자가 특정 해시태그를 가지고 있는지 확인
    boolean existsByAccountAndUserHashtagAndIsDeletedFalse(Account account, UserHashtag userHashtag);
}
