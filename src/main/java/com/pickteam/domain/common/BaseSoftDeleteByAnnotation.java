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

    @Column(name = "deleted_at")
    protected LocalDateTime deletedAt;

    @PreRemove
    public void onSoftDelete() {
        this.deletedAt = LocalDateTime.now(); // Hibernate가 해주지 않는다.
    }

    /** 수동 삭제 처리 (필요시 사용) */
    public void markDeleted() {
        this.deletedAt = LocalDateTime.now();
        // isDeleted는 Hibernate @SoftDelete가 자동으로 관리
    }

    /** 복구 처리 */
    public void restore() {
        this.deletedAt = null;
        // isDeleted는 Hibernate @SoftDelete가 자동으로 관리
    }
}
