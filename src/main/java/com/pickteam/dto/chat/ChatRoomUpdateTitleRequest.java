package com.pickteam.dto.chat;

import com.pickteam.domain.enums.ChatRoomType;
import jakarta.mail.Message;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
@Data
public class ChatRoomUpdateTitleRequest {

    @NotNull(message = "workspace id는 필수입니다.")
    private Long workspaceId;

    @NotNull(message = "채팅방 id는 필수입니다.")
    private Long chatRoomId;

    @NotBlank(message = "채팅방 이름은 필수입니다.")
    private String newName;

}


