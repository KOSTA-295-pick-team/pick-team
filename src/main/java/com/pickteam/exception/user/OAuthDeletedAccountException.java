package com.pickteam.exception.user;

import com.pickteam.domain.user.Account;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * OAuth 로그인 시 삭제된 계정으로 로그인 시도 시 발생하는 예외
 */
@Getter
public class OAuthDeletedAccountException extends RuntimeException {

    private final Long accountId;
    private final LocalDateTime deletedAt;
    private final LocalDateTime permanentDeletionDate;
    private final String provider;

    public OAuthDeletedAccountException(String message, Account deletedAccount) {
        super(message);
        this.accountId = deletedAccount.getId();
        this.deletedAt = deletedAccount.getDeletedAt();
        this.permanentDeletionDate = deletedAccount.getPermanentDeletionDate();
        this.provider = deletedAccount.getProvider() != null ? deletedAccount.getProvider().name() : null;
    }

    public OAuthDeletedAccountException(String message, Long accountId, LocalDateTime deletedAt,
            LocalDateTime permanentDeletionDate, String provider) {
        super(message);
        this.accountId = accountId;
        this.deletedAt = deletedAt;
        this.permanentDeletionDate = permanentDeletionDate;
        this.provider = provider;
    }
}
