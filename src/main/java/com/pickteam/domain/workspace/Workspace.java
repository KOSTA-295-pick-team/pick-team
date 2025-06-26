package com.pickteam.domain.workspace;

import com.pickteam.domain.common.BaseSoftDeleteSupportEntity;
import com.pickteam.domain.user.Account;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workspace extends BaseSoftDeleteSupportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String password;

    @Column(unique = true)
    private String url;
    
    @Column(name = "icon_url")
    private String iconUrl;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account owner;

    @OneToMany(mappedBy = "workspace")
    private List<WorkspaceMember> members = new ArrayList<>();
    
    @PrePersist
    public void generateInviteCode() {
        if (this.url == null) {
            this.url = UUID.randomUUID().toString().substring(0, 8);
        }
    }
}
