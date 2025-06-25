package com.pickteam.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pickteam.domain.user.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByEmail(String email);

    // TODO : 기타 쿼리 매서드

}
