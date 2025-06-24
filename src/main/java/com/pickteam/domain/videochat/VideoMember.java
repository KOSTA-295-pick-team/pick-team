package com.pickteam.domain.videochat;

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
public class VideoMember extends BaseSoftDeleteSupportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;

    @ManyToOne(optional = false)
    private Account account;

    @ManyToOne(optional = false)
    private VideoChannel videoChannel;

}
