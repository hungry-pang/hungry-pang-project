package com.example.hungrypangproject.domain.point.entity;

import lombok.Getter;

@Getter
public enum PointEnum {
    USE("USE","사용 가능"),
    HOLDING("HOLDING","누적 대기"),
    SAVE("SAVE","누적"),
    EXPIRE("EXPIRE","만료");

    private final String statusCode;
    private final String description;

    PointEnum(String statusCode, String description) {
        this.statusCode = statusCode;
        this.description = description;
    }
}
