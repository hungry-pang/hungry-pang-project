package com.example.hungrypangproject.domain.payment.dto;

import com.example.hungrypangproject.domain.payment.entity.Payment;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class PaymentDetailResponse {

    private final Long paymentId;
    private final String dbPaymentId;
    private final String portOnePaymentId;
    private final Long orderId;
    private final BigDecimal totalAmount;
    private final BigDecimal pointsToUse;
    private final String status;

    public PaymentDetailResponse(
            Long paymentId,
            String dbPaymentId,
            String portOnePaymentId,
            Long orderId,
            BigDecimal totalAmount,
            BigDecimal pointsToUse,
            String status
    ) {
        this.paymentId = paymentId;
        this.dbPaymentId = dbPaymentId;
        this.portOnePaymentId = portOnePaymentId;
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.pointsToUse = pointsToUse;
        this.status = status;
    }

    public static PaymentDetailResponse from(Payment payment) {
        return new PaymentDetailResponse(
                payment.getId(),
                payment.getDbPaymentId(),
                payment.getPaymentId(),
                payment.getOrder().getId(),
                payment.getTotalAmount(),
                payment.getPointsToUse(),
                payment.getStatus().name()
        );
    }
}

