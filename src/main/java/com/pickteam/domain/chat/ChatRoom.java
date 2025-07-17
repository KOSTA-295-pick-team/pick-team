package com.pickteam.domain.chat;

import com.pickteam.domain.common.BaseSoftDeleteSupportEntity;
import com.pickteam.domain.enums.ChatRoomType;
import com.pickteam.domain.workspace.Workspace;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom extends BaseSoftDeleteSupportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private ChatRoomType type;

    @ManyToOne(optional = false)
    private Workspace workspace;


    @OneToMany(mappedBy = "chatRoom", orphanRemoval = true)
    private List<ChatMessage> chatMessages = new ArrayList<>();

    @OneToMany(mappedBy = "chatRoom", orphanRemoval = true)
    private List<ChatMember> chatMembers = new ArrayList<>();


}
