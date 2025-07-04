package com.pickteam.domain.team;

import com.pickteam.domain.common.BaseSoftDeleteByAnnotation;
import com.pickteam.domain.common.BaseSoftDeleteSupportEntity;
import com.pickteam.domain.common.BaseTimeEntity;
import com.pickteam.domain.user.Account;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMember extends BaseSoftDeleteSupportEntity {
    //팀 탈퇴(혹은 추방)된 멤버에 대한 정보가 남아있어야 하므로... soft-delete 처리가 되어야 함

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
    
    // 팀 멤버 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "team_status")
    @Builder.Default
    private TeamStatus teamStatus = TeamStatus.ACTIVE;
    
    /**
     * 팀 내 역할
     */
    public enum TeamRole {
        LEADER,  // 팀장
        MEMBER   // 팀원
    }
    
    /**
     * 팀 멤버 상태
     */
    public enum TeamStatus {
        ACTIVE,  // 활성 멤버
        LEFT     // 탈퇴한 멤버
    }
}
