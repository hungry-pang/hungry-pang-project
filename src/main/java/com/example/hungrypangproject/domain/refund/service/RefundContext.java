package com.example.hungrypangproject.domain.refund.service;

import java.math.BigDecimal;
import java.util.UUID;

public record RefundContext(
        Long paymentId,
        String dbPaymentId,
        BigDecimal refundAmount,
        Long orderId,
        UUID orderNum,
        String reason,
        String refundGroupId
) {
}

