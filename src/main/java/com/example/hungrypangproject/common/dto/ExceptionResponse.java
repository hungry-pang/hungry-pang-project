package com.example.hungrypangproject.common.dto;

import com.example.hungrypangproject.common.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ExceptionResponse {

    private final int errorCode;
    private final String message;
    private final String path;
    private final LocalDateTime time;

    public static ExceptionResponse from(int errorCode, String message, String path) {
        return ExceptionResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .time(LocalDateTime.now())
                .build();
    }
}
