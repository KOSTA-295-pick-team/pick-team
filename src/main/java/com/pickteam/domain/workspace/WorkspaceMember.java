package com.pickteam.domain.workspace;

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
public class WorkspaceMember extends BaseTimeEntity {
    //현재 멤버에 대한 생성정보만 사용할 것이므로 BaseTimeEntity를 상속받는다.
    //입-퇴장 로그는 이 테이블에서 Soft-Delete 처리하지 않고, 필요 시 별도의 로그를 통해 관리한다. {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;

    @ManyToOne(optional = false)
    private Workspace workspace;

    @ManyToOne(optional = false)
    private Account account;
}
