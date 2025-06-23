package com.pickteam.domain.chat;

import com.pickteam.domain.workspace.Workspace;
import com.pickteam.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private ChatRoomType type;

    @ManyToOne(optional = false)
    private Workspace workspace;
}
