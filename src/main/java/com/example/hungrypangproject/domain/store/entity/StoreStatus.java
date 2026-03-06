package com.example.hungrypangproject.domain.store.entity;

import lombok.Getter;

@Getter
public enum StoreStatus {
    OPEN("OPEN", "영업 중"),
    CLOSED("CLOSED", "영업 종료"),
    PREPARING("PREPARING", "영업 준비 중");

    private final String statusCode;
    private final String description;

    StoreStatus(String statusCode, String description) {
        this.statusCode = statusCode;
        this.description = description;
    }
}
