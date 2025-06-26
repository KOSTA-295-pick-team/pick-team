package com.pickteam.domain.workspace;

import com.pickteam.domain.common.BaseSoftDeleteByAnnotation;
import com.pickteam.domain.user.Account;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"workspace_id", "account_id"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceMember extends BaseSoftDeleteByAnnotation {
    //유저가 탈퇴하거나 추방되어도 관련 정보가 삭제되면 안된다. soft-delete 처리한다.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;

    @ManyToOne(optional = false)
    private Workspace workspace;

    @ManyToOne(optional = false)
    private Account account;
    
    // mvc 버전에서 추가된 필드들
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Builder.Default
    private MemberRole role = MemberRole.MEMBER;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private MemberStatus status = MemberStatus.ACTIVE;
    
    public enum MemberRole {
        OWNER, ADMIN, MEMBER
    }
    
    public enum MemberStatus {
        ACTIVE, BANNED, LEFT
    }
}
