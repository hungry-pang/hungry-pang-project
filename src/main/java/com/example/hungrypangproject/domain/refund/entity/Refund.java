package com.example.hungrypangproject.domain.refund.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "refunds")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long paymentId;

    private BigDecimal refundAmount;

    private String reason;

    private RefundStatus status;

    // PortOne에서 반환하는 환불 ID
    private String portOneRefundId;
    private String refundGroupId;
    private LocalDateTime refundedAt;

    @Builder
    private Refund(Long paymentId, BigDecimal refundAmount, String reason, RefundStatus status, String portOneRefundId, String refundGroupId) {
        this.paymentId = paymentId;
        this.refundAmount = refundAmount;
        this.reason = reason;
        this.status = status;
        this.portOneRefundId = portOneRefundId;
        this.refundGroupId = refundGroupId;
        this.refundedAt = LocalDateTime.now();
    }
}
