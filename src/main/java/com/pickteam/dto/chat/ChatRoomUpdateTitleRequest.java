package com.pickteam.dto.chat;

import com.pickteam.domain.enums.ChatRoomType;
import jakarta.mail.Message;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomUpdateTitleRequest {

    @NotBlank(message = "채팅방 이름은 필수입니다.")
    private String newName;

}


