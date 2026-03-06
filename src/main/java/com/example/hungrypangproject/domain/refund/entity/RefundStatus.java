package com.example.hungrypangproject.domain.refund.entity;

import lombok.Getter;

@Getter
public enum RefundStatus {

    PENDING("PENDING", "환불대기"),
    COMPLETED("COMPLETED", "환불완료"),
    FAILED("FAILED","환불실패");

    private final String statusCode;
    private final String description;

    RefundStatus(String statusCode, String description) {
        this.statusCode = statusCode;
        this.description = description;
    }
}
