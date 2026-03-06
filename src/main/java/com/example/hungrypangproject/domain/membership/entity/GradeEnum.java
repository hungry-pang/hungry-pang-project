package com.example.hungrypangproject.domain.membership.entity;

import lombok.Getter;

@Getter
public enum GradeEnum {
    NORMAL("NORMAL","일반회원"),
    VIP("VIP", "우수회원"),
    VVIP("VVIP","최우수 회원");

    private final String statusCode;
    private final String description;

    GradeEnum(String statusCode, String description) {
        this.statusCode = statusCode;
        this.description = description;
    }
}
