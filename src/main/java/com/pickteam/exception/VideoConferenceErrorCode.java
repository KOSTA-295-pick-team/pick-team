package com.pickteam.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum VideoConferenceErrorCode {

    CHANNELS_NOT_FOUND(HttpStatus.NOT_FOUND, "channels not found", "채널을 찾지 못했습니다"),
    CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "channel not found", "해당하는 채널을 찾지 못했습니다"),
    MEMBERS_NOT_FOUND(HttpStatus.NOT_FOUND, "members not found", "채널의 멤버를 찾지 못했습니다"),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "member not found", "채널에서 해당 멤버를 찾지 못했습니다");

    private final HttpStatus httpStatus;
    private final String title;
    private final String message;
    }
