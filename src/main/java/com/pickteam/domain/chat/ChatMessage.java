package com.pickteam.domain.chat;

import com.pickteam.domain.common.BaseSoftDeleteByAnnotation;
import com.pickteam.domain.common.BaseSoftDeleteSupportEntity;
import com.pickteam.domain.user.Account;
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
public class ChatMessage extends BaseSoftDeleteByAnnotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @ManyToOne(optional = false)
    private ChatRoom chatRoom;

    @ManyToOne(optional = false)
    private Account account;

    @OneToMany(mappedBy = "chatMessage", orphanRemoval = true)
    private List<ChatAttach> attachments = new ArrayList<>();

    // Soft-Delete 시, @SoftDelete 어노테이션은 자식객체에게 soft-delete를 전파해주지 않는다.
    // Soft-Delete 동작 전파를 위해 OnSoftDelete를 오버라이드 (검증 필요함)

    @Override
    public void onSoftDelete() {
        super.onSoftDelete();
        attachments.forEach(BaseSoftDeleteByAnnotation::markDeleted);
    }


}
