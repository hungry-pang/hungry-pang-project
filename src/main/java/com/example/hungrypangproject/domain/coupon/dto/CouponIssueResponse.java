package com.example.hungrypangproject.domain.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CouponIssueResponse {

    private Long couponId;
    private int issuedQuantity;
    private int remainingQuantity;
}

