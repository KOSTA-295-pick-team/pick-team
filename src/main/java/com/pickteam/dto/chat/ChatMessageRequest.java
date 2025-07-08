package com.pickteam.dto.chat;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

@Data
public class ChatMessageRequest {
    @NotNull
    private Long senderId;      // 보낸 사람 ID (Account)
    @NotNull
    private String senderName; // 보낸 사람 이름
    @NotNull
    private String content;     // 메시지 본문
 }