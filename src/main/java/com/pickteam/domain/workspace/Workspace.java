package com.pickteam.domain.workspace;

import com.pickteam.domain.common.BaseSoftDeleteSupportEntity;
import com.pickteam.domain.user.Account;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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
    
    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @OneToMany(mappedBy = "workspace")
    private List<WorkspaceMember> members = new ArrayList<>();
}
