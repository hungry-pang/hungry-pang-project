package com.example.hungrypangproject.domain.refund.dto;

import com.example.hungrypangproject.domain.refund.entity.Refund;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class RefundDetailResponse {

    private final Long refundId;
    private final Long paymentId;
    private final String dbPaymentId;
    private final Long orderId;
    private final BigDecimal refundAmount;
    private final String reason;
    private final String status;
    private final String portOneRefundId;
    private final String refundGroupId;
    private final LocalDateTime refundedAt;

    public RefundDetailResponse(
            Long refundId,
            Long paymentId,
            String dbPaymentId,
            Long orderId,
            BigDecimal refundAmount,
            String reason,
            String status,
            String portOneRefundId,
            String refundGroupId,
            LocalDateTime refundedAt
    ) {
        this.refundId = refundId;
        this.paymentId = paymentId;
        this.dbPaymentId = dbPaymentId;
        this.orderId = orderId;
        this.refundAmount = refundAmount;
        this.reason = reason;
        this.status = status;
        this.portOneRefundId = portOneRefundId;
        this.refundGroupId = refundGroupId;
        this.refundedAt = refundedAt;
    }

    public static RefundDetailResponse of(Refund refund, String dbPaymentId, Long orderId) {
        return new RefundDetailResponse(
                refund.getId(),
                refund.getPaymentId(),
                dbPaymentId,
                orderId,
                refund.getRefundAmount(),
                refund.getReason(),
                refund.getStatus().name(),
                refund.getPortOneRefundId(),
                refund.getRefundGroupId(),
                refund.getRefundedAt()
        );
    }
}

