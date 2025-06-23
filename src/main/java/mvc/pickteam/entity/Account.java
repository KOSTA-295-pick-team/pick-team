package mvc.pickteam.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "account")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "email", unique = true, nullable = false)
    private String email;
    
    @Column(name = "password", nullable = false)
    private String password;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "age")
    private Integer age;
    
    @Column(name = "mbti", length = 4)
    private String mbti;
    
    @Column(name = "introduction", columnDefinition = "TEXT")
    private String introduction;
    
    @Column(name = "disposition", columnDefinition = "TEXT")
    private String disposition;
    
    @Column(name = "portfolio")
    private String portfolio;
    
    @Column(name = "likes", columnDefinition = "TEXT")
    private String likes;
    
    @Column(name = "dislikes", columnDefinition = "TEXT")
    private String dislikes;
    
    @Column(name = "profile_image")
    private String profileImage;
    
    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 워크스페이스 멤버십 관계
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WorkspaceMember> workspaceMemberships;
} 