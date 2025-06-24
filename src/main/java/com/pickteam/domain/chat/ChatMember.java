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
public class ChatMember extends BaseTimeEntity {
    //현재 멤버에 대한 생성정보만 사용할 것이므로 BaseTimeEntity를 상속받는다.
    //입-퇴장 로그는 이 테이블에서 Soft-Delete 처리하지 않고, 필요 시 별도의 로그를 통해 관리한다.
    //(로그성 정보가 이 테이블에 쌓일 경우, 입퇴장이 반복때마다 soft-delete 정보가 쌓여서 테이블이 무거워진다.)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private ChatRoom chatRoom;

    @ManyToOne(optional = false)
    private Account account;
}
