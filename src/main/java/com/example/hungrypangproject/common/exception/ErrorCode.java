package com.example.hungrypangproject.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // store
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 식당을 찾을 수 없습니다."),

    // menu
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 메뉴를 찾을 수 없습니다."),
    DUPLICATE_MENU_NAME(HttpStatus.BAD_REQUEST, "이미 등록된 메뉴명입니다."),
    INVALID_MENU_STATUS(HttpStatus.BAD_REQUEST, "변경할 수 없는 메뉴 상태입니다.");

    private final HttpStatus status;
    private final String message;
}
