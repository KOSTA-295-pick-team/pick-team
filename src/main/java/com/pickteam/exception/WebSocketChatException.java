package com.pickteam.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class WebSocketChatException extends RuntimeException {
    private final WebSocketChatErrorCode webSocketChatErrorCode;
}
