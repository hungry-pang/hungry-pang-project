package com.example.hungrypangproject.domain.menu.entity;

import lombok.Getter;

@Getter
public enum MenuStatus {
    SALE("SALE", "판매 중"),
    SOLDOUT("SOLDOUT", "품절"),
    PREPARING("PREPARING", "판매 준비 중");

    private final String statusCode;
    private final String description;

    MenuStatus(String statusCode, String description) {
        this.statusCode = statusCode;
        this.description = description;
    }
}
