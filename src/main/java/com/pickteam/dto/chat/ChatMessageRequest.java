package com.pickteam.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.NotNull;  // ✅ 추가

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {
    @NotNull
    private Long senderId;      // 보낸 사람 ID (Account)
    @NotNull
    private String senderName; // 보낸 사람 이름
    @NotNull
    private String content;     // 메시지 본문
 }