package com.pickteam.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class VideoConferenceException extends Exception {
    private final VideoConferenceErrorCode videoConferenceErrorCode;
}
