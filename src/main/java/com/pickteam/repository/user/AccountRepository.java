package com.pickteam.repository.user;

import com.pickteam.domain.user.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}