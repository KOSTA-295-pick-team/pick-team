package mvc.pickteam.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "workspace_member", 
       uniqueConstraints = {@UniqueConstraint(columnNames = {"workspace_id", "account_id"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class WorkspaceMember {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Builder.Default
    private MemberRole role = MemberRole.MEMBER;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private MemberStatus status = MemberStatus.ACTIVE;
    
    @CreatedDate
    @Column(name = "joined_at")
    private LocalDateTime joinedAt;
    
    public enum MemberRole {
        OWNER, ADMIN, MEMBER
    }
    
    public enum MemberStatus {
        ACTIVE, BANNED, LEFT
    }
} 