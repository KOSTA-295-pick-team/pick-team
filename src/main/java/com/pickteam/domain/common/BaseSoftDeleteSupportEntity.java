package com.pickteam.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Soft-Delete를 수동 관리하는 코드 (Deprecated)
 */

@Getter
@MappedSuperclass
public abstract class BaseSoftDeleteSupportEntity extends BaseTimeEntity {

    @Column(name="is_deleted", nullable = false)
    protected Boolean isDeleted = false;

    @Column(name="deleted_at")
    protected LocalDateTime deletedAt;

    public void markDeleted() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.isDeleted = false;
        this.deletedAt = null;
    }

    public boolean isActive() {
        return !Boolean.TRUE.equals(this.isDeleted);
    }
}
