package com.pickteam.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 공통적으로 사용되는 타임스탬프 관련 컬럼을 MappedSuperclass 어노테이션을 통해 관리
 * updated
 */
@MappedSuperclass
@Getter
public abstract class BaseTimeEntity {


    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    //EntityManager.persist() 호출 시 실행, 해당 컬럼의 값을 현재 시간으로 수정 (CreatedDate와 같은 역할)
    protected void onCreate() {
        this.createdAt = this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    // flush/commit 시 실행, 해당 컬럼의 값을 현재 시간으로 수정 (LastModifiedDate와 같은 역할)
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}