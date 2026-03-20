package com.example.hungrypangproject.domain.payment.service;

import java.math.BigDecimal;

public record PaymentVerifyContext(
        String dbPaymentId,
        BigDecimal finalAmount,
        boolean alreadyProcessed
) {

    public static PaymentVerifyContext of(String dbPaymentId, BigDecimal finalAmount) {
        return new PaymentVerifyContext(dbPaymentId, finalAmount, false);
    }

    public static PaymentVerifyContext alreadyProcessed(String dbPaymentId, BigDecimal finalAmount) {
        return new PaymentVerifyContext(dbPaymentId, finalAmount, true);
    }
}

