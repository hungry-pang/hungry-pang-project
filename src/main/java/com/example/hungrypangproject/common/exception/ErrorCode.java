package com.example.hungrypangproject.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    //STORE
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 식당을 찾을 수 없습니다."),
    STORE_NOT_OPEN(HttpStatus.NOT_FOUND, "영업 중인 식당이 아닙니다."),

    //ORDER
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    ORDER_CANCEL_FORBIDDEN(HttpStatus.FORBIDDEN, "본인 주문만 취소할 수 있습니다."),
    ORDER_BELOW_MINIMUM(HttpStatus.BAD_REQUEST, "최소 주문금액을 충족하지 않습니다."),
    ORDER_NOT_CANCELABLE(HttpStatus.BAD_REQUEST, "대기 중인 주문만 취소할 수 있습니다."),

    //MENU
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 메뉴를 찾을 수 없습니다."),
    MENU_SOLD_OUT(HttpStatus.BAD_REQUEST, "품절된 메뉴입니다."),
    MENU_DUPLICATE_NAME(HttpStatus.BAD_REQUEST, "이미 등록된 메뉴명입니다."),

    //POINT
    POINT_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "포인트가 부족합니다."),
    POINT_EXCEED_LIMIT(HttpStatus.BAD_REQUEST, "포인트는 결제금액의 10% 이하만 사용 가능합니다."),

    //MEMBER
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "유저를 찾을수 없습니다."),

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
}
