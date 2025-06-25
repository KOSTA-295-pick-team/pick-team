package com.pickteam.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import com.pickteam.domain.user.Account;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    // ID 중복검사
    boolean existsByEmail(String email);

    // 이메일로 사용자 찾기
    Optional<Account> findByEmail(String email);

    // 이메일과 비밀번호로 사용자 찾기 (로그인용)
    Optional<Account> findByEmailAndPassword(String email, String password);

    // 삭제되지 않은 사용자만 조회
    Optional<Account> findByIdAndDeletedAtIsNull(Long id);

    // 전체 활성 사용자 조회
    java.util.List<Account> findAllByDeletedAtIsNull();
}
