package com.example.hungrypangproject.domain.delivery.entity;

import lombok.Getter;

@Getter
public enum DeliveryStatus {
    PICKEDUP("PICKEDUP", "픽업 됨"),
    DELIVERING("DELIVERING", "배달 중"),
    COMPLERED("COMPLETED", "배달 완료"),
    SEARCHING("SEARCHING", "기사 탐색");

    private final String statusCode;
    private final String description;

    DeliveryStatus(String statusCode, String description) {
        this.statusCode = statusCode;
        this.description = description;
    }
}
