package com.pickteam.domain.videochat;

import com.pickteam.domain.common.BaseSoftDeleteByAnnotation;
import com.pickteam.domain.common.BaseSoftDeleteSupportEntity;
import com.pickteam.domain.common.BaseTimeEntity;
import com.pickteam.domain.workspace.Workspace;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoChannel extends BaseSoftDeleteSupportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(optional = false,fetch = FetchType.LAZY)
    private Workspace workspace;

    @OneToMany(mappedBy = "videoChannel",cascade = CascadeType.REMOVE)//채널에 속한 멤버가 존재해도 채널을 삭제할수 있게끔 함
    private List<VideoMember> members = new ArrayList<>();

}
