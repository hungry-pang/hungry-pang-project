package com.example.hungrypangproject.domain.order.entity;

import lombok.Getter;

@Getter
public enum OrderStatus {
    WATING("WATING", "주문 대기"),
    PREPARING("PREPARING", "준비"),
    COMPLETED("COMPLETED", "주문 완료"),
    REFUNDED("REFUNDED", "환불");

    private final String statusCode;
    private final String description;

    OrderStatus(String statusCode, String description) {
        this.statusCode = statusCode;
        this.description = description;
    }
}
