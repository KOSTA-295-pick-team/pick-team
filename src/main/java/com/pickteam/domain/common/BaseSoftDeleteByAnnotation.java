package com.pickteam.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PreRemove;
import lombok.Getter;
import org.hibernate.annotations.SoftDelete;

import java.time.LocalDateTime;

/**
 * Hibernate 6.4+ @SoftDelete 기반 soft-delete 추상 엔티티
 */
@Getter
@MappedSuperclass
@SoftDelete(columnName = "is_deleted")
public abstract class BaseSoftDeleteByAnnotation extends BaseTimeEntity {

    @Column(name = "is_deleted", nullable = false)
    protected Boolean isDeleted = false;

    @Column(name = "deleted_at")
    protected LocalDateTime deletedAt;

    @PreRemove
    public void onSoftDelete() {
        this.deletedAt = LocalDateTime.now(); // Hibernate가 해주지 않는다.
    }

    /** isDeleted 컬럼의 값을 수동 관리 필요할 경우를 위한 수동설정 메소드 (Hibernate가 관리해주지 않을 경우) */
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
