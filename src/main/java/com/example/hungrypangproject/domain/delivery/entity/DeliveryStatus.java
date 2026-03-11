package com.example.hungrypangproject.domain.delivery.entity;

import lombok.Getter;

@Getter
public enum DeliveryStatus {
    PENDING("PENDING", "배달 대기"),
    DELIVERING("DELIVERING", "배달 중"),
    COMPLETED("COMPLETED", "배달 완료");

    private final String statusCode;
    private final String description;

    DeliveryStatus(String statusCode, String description) {
        this.statusCode = statusCode;
        this.description = description;
    }
}
