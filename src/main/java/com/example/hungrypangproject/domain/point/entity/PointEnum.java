package com.example.hungrypangproject.domain.point.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointEnum {
    USE("USE","사용가능"),
    HOLDING("HOLDING","대기중"),
    SAVE("SAVE","누적"),
    EXPIRE("EXPIRE","만료");

    private final String statusCode;
    private final String description;

    PointEnum(String statusCode, String description) {
        this.statusCode = statusCode;
        this.description = description;
    }
}
