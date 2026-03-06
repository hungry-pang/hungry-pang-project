package com.example.hungrypangproject.domain.review.entity;

import lombok.Getter;

@Getter
public enum ReviewStatus {
    EXPOSED("EXPOSED", "리뷰 노출"),
    HIDDEN("HIDDEN", "리뷰 숨김"),
    DELETED("DELETED", "리뷰 삭제");

    private final String statusCode;
    private final String description;

    ReviewStatus(String statusCode, String description) {
        this.statusCode = statusCode;
        this.description = description;
    }
}
