package com.pickteam.domain.team;

import com.pickteam.domain.common.BaseSoftDeleteByAnnotation;
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
public class TeamMember extends BaseSoftDeleteByAnnotation {
    //팀 탈퇴(혹은 추방)된 멤버에 대한 정보가 남아있어야 하므로... soft-delete 처리가 되어야 함

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Team team;

    @ManyToOne(optional = false)
    private Account account;
}
