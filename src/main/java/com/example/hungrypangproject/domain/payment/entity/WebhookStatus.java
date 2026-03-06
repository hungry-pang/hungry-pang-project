package com.example.hungrypangproject.domain.payment.entity;

import lombok.Getter;

@Getter
public enum WebhookStatus {
    RECEIVED("RECEIVED", "수신"),
    PROCESSED("PROCESSED", "처리완료"),
    FAILED("FAILED", "처리실패");

    private final String statusCode;
    private final String description;

    WebhookStatus(String statusCode, String description)
    {
        this.statusCode = statusCode;
        this.description = description;
    }
}
