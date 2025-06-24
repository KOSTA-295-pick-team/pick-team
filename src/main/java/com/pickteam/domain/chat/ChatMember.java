package com.pickteam.domain.chat;

import com.pickteam.domain.common.BaseSoftDeleteByAnnotation;
import com.pickteam.domain.common.BaseTimeEntity;
import com.pickteam.domain.user.Account;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMember extends BaseSoftDeleteByAnnotation {
    //Soft-Delete 방식으로 설계변경

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private ChatRoom chatRoom;

    @ManyToOne(optional = false)
    private Account account;
}
