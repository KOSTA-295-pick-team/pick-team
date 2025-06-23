package com.pickteam.domain.notification;

import com.pickteam.domain.common.BaseTimeEntity;
import com.pickteam.domain.user.Account;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLog extends BaseTimeEntity {
    //알람은 soft-delete가 필요없는 정보다. 유저가 확인하면 날아가도 무방하다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @Column(nullable = false)
    private Boolean isRead = false;

    @ManyToOne(optional = false)
    private Account account;
}
