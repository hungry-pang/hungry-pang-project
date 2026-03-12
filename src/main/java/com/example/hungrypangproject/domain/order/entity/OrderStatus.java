package com.example.hungrypangproject.domain.order.entity;

import lombok.Getter;

@Getter
public enum OrderStatus {
    WAITING("WAITING", "주문 확인"),
    PREPARING("PREPARING", "조리 중"),
    COMPLETED("COMPLETED", "조리 완료"),
    REFUNDED("REFUNDED", "환불"),
    CANCELLED("CANCELLED", "주문 취소");

    private final String statusCode;
    private final String description;

    OrderStatus(String statusCode, String description) {
        this.statusCode = statusCode;
        this.description = description;
    }
}
