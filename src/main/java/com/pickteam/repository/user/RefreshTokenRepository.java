package com.pickteam.repository.user;

import com.pickteam.domain.user.RefreshToken;
import com.pickteam.domain.user.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByAccount(Account account);

    void deleteByAccount(Account account);
}
