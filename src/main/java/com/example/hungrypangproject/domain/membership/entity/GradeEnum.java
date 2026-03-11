package com.example.hungrypangproject.domain.membership.entity;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum GradeEnum {
    NORMAL("NORMAL","일반회원",BigDecimal.valueOf(0),BigDecimal.valueOf(0.01)),
    VIP("VIP", "우수회원",BigDecimal.valueOf(100000),BigDecimal.valueOf(0.01)),
    VVIP("VVIP","최우수 회원",BigDecimal.valueOf(300000),BigDecimal.valueOf(0.01));

    private final String statusCode;
    private final String description;
    private final BigDecimal minAmount;
    private final BigDecimal earnRate;

    GradeEnum(
            String statusCode,
            String description,
            BigDecimal minAmount,
            BigDecimal earnRate
    ) {
        this.statusCode = statusCode;
        this.description = description;
        this.minAmount = minAmount;
        this.earnRate = earnRate;
    }

    // 금액에 따른 등급 판별
    public static GradeEnum determineGrade (BigDecimal totalPrice) {
        if(totalPrice.compareTo(VVIP.minAmount) >= 0) return VVIP;
        if(totalPrice.compareTo(VIP.minAmount) >= 0) return VIP;
        return NORMAL;
    }
}
