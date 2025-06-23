package com.pickteam.domain.videochat;

import com.pickteam.domain.common.BaseSoftDeleteByAnnotation;
import com.pickteam.domain.common.BaseTimeEntity;
import com.pickteam.domain.workspace.Workspace;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoChannel extends BaseTimeEntity {
    //생성되어있는 현재 채널 목록 테이블은 속도를 위해 soft-delete 처리하지 않는다.
    //생성 및 종료 내역이 필요할 경우 별도 테이블로 관리한다.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(optional = false)
    private Workspace workspace;
}
