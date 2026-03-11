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

    STORE_ONLY_SELLER(HttpStatus.FORBIDDEN, "판매자만 식당을 등록할 수 있습니다."),
    STORE_FORBIDDEN(HttpStatus.FORBIDDEN, "식당에 대한 권한이 없습니다."),

    //ORDER
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    ORDER_CANCEL_FORBIDDEN(HttpStatus.FORBIDDEN, "본인 주문만 취소할 수 있습니다."),
    ORDER_BELOW_MINIMUM(HttpStatus.BAD_REQUEST, "최소 주문금액을 충족하지 않습니다."),
    ORDER_NOT_CANCELABLE(HttpStatus.BAD_REQUEST, "대기 중인 주문만 취소할 수 있습니다."),
    ORDER_NOT_CHANGEABLE(HttpStatus.BAD_REQUEST, "이미 완료되거나 환불된 주문입니다."),
    ORDER_STATUS_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 주문 상태입니다."),

    //MENU
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 메뉴를 찾을 수 없습니다."),
    MENU_SOLD_OUT(HttpStatus.BAD_REQUEST, "품절된 메뉴입니다."),
    MENU_DUPLICATE_NAME(HttpStatus.BAD_REQUEST, "이미 등록된 메뉴명입니다."),

    //POINT
    POINT_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "포인트가 부족합니다."),
    POINT_EXCEED_LIMIT(HttpStatus.BAD_REQUEST, "포인트는 결제금액의 10% 이하만 사용 가능합니다."),
    POINT_NOT_HOLDING(HttpStatus.BAD_REQUEST, "적립 대기중인 포인트가 없습니다."),

    //MEMBER
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "유저를 찾을수 없습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.NOT_FOUND, "중복된 이메일입니다."),


    INVALID_PASSWORD(HttpStatus.BAD_REQUEST,"비밀번호가 일치하지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED,"유효하지 않은 JWT 토큰입니다."),

    //REVIEW
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."),
    REVIEW_FORBIDDEN(HttpStatus.FORBIDDEN, "본인이 작성한 리뷰만 수정 또는 삭제할 수 있습니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "한 주문에는 하나의 리뷰만 작성할 수 있습니다."),
    REVIEW_ORDER_FORBIDDEN(HttpStatus.FORBIDDEN, "본인의 주문에 대해서만 리뷰를 작성할 수 있습니다."),
    REVIEW_ORDER_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "주문 완료된 경우에만 리뷰를 작성할 수 있습니다."),
    REVIEW_STATUS_FORBIDDEN(HttpStatus.FORBIDDEN, "관리자만 리뷰 상태를 변경할 수 있습니다."),

    //PAYMENT
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 정보를 찾을 수 없습니다."),
    PAYMENT_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "이미 처리된 결제입니다."),
    PAYMENT_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, "결제 검증에 실패했습니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제 금액이 일치하지 않습니다."),
    PAYMENT_INFO_MISMATCH(HttpStatus.BAD_REQUEST, "결제 정보가 일치하지 않습니다."),
    PAYMENT_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "결제가 완료되지 않았습니다."),
    PAYMENT_INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "최종 결제 금액은 0보다 작을 수 없습니다."),
    PAYMENT_DUPLICATE(HttpStatus.BAD_REQUEST, "이미 진행 중이거나 완료된 결제가 있습니다."),
    PORTONE_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "PortOne API 호출 중 오류가 발생했습니다."),
  
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
}
