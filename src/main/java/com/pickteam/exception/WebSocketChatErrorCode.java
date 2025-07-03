package com.pickteam.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum WebSocketChatErrorCode {

    INVALID_USER(HttpStatus.BAD_REQUEST,"Wrong user","유효한 사용자가 아닙니다"),
    INVALID_CHANNEL(HttpStatus.BAD_REQUEST,"Invalid channel","유효한 채팅 채널이 아닙니다"),
    CANNOT_ACCESS_CHANNEL(HttpStatus.FORBIDDEN,"Can't access channel","해당 채팅 채널에 접근할 권한이 없습니다");
    private final HttpStatus httpStatus;
    private final String title;
    private final String message;
}
