package com.pickteam.domain.chat;

import com.pickteam.domain.common.BaseSoftDeleteByAnnotation;
import com.pickteam.domain.enums.ChatRoomType;
import com.pickteam.domain.workspace.Workspace;
import com.pickteam.domain.common.BaseTimeEntity;
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
public class ChatRoom extends BaseSoftDeleteByAnnotation {

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

    @Override
    public void onSoftDelete() {
        super.onSoftDelete();
        chatMessages.forEach(BaseSoftDeleteByAnnotation::markDeleted);

    }



}
