package com.example.hungrypangproject.domain.refund.consts;

import lombok.Getter;

@Getter

public enum RefundStatus {

    PENDING("PENDING"),
    COMPLETED("COMPLETED"),
    FAILED("FAILED");

    private final String statusName;
    RefundStatus(String statusName) {this.statusName = statusName;}
}
