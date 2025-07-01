package com.pickteam.domain.notification;

import com.pickteam.domain.common.BaseSoftDeleteByAnnotation;
import com.pickteam.domain.common.BaseSoftDeleteSupportEntity;
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
public class NotificationLog extends BaseSoftDeleteSupportEntity {
    //하드삭제 -> 소프트삭제로 설계변경. 유저가 확인하면 삭제되어도 무방하지만 운영상 관리 필요성 생길 수 있음
    //즉시 삭제되지 않고 soft-delete 처리한 뒤 날짜기반으로 배치 삭제해도 무방

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @Column(nullable = false)
    private Boolean isRead = false;

    @ManyToOne(optional = false)
    private Account account;
}
