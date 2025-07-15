// ChatRoomCreateRequest.java
package com.pickteam.dto.chat;

import com.pickteam.domain.enums.ChatRoomType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomCreateRequest {
    @NotNull(message = "워크스페이스 ID는 필수입니다.")
    private Long workspaceId;

    @NotBlank(message = "채팅방 이름은 필수입니다.")
    private String name;

    @NotNull(message = "채팅방 타입은 필수입니다.")
    private ChatRoomType type;

    @NotNull(message="채팅에 참여한 멤버가 없습니다.")
    private List<Long> chatMemberIdList;

}