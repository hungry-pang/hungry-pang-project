package com.example.hungrypangproject.domain.payment.entity;

import lombok.Getter;

@Getter
public enum PaymentStatus {

    PENDING("PENDING", "결제대기"),
    VERIFYING("VERIFYING", "결제검증중"),
    PAID("PAID", "결제완료"),
    FAIL("FAIL", "결제실패"),
    REFUNDING("REFUNDING", "환불처리중"),
    REFUND("REFUND", "환불");

    private final String statusCode;
    private final String description;

    PaymentStatus(String statusCode, String description) {
        this.statusCode = statusCode;
        this.description = description;
    }
}
