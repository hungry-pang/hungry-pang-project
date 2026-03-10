package com.example.hungrypangproject.domain.member.entity;

public enum MemberRoleEnum {
    ROLE_USER("USER", "회원"),
    ROLE_SELLER("SELLER", "판매자"),
    ROLE_RAIDER("RAIDER", "배달원");

    private final String statusCode;
    private final String description;

    MemberRoleEnum(String statusCode, String description) {
        this.statusCode = statusCode;
        this.description = description;
    }
}
