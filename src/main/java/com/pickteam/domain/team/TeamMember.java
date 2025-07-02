package com.pickteam.domain.team;

import com.pickteam.domain.common.BaseSoftDeleteSupportEntity;
import com.pickteam.domain.user.Account;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMember extends BaseSoftDeleteSupportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Team team;

    @ManyToOne(optional = false)
    private Account account;
    
    // 팀 내 역할
    @Enumerated(EnumType.STRING)
    @Column(name = "team_role")
    @Builder.Default
    private TeamRole teamRole = TeamRole.MEMBER;

    //차단 여부
    private boolean isBlocked;

    /**
     * 팀 내 역할
     */
    public enum TeamRole {
        LEADER,  // 팀장
        MEMBER   // 팀원
    }
}