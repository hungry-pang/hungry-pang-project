package com.example.hungrypangproject.domain.refund.entity;

import com.example.hungrypangproject.domain.payment.entity.Payment;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    private BigDecimal refundAmount;

    private String reason;

    @Enumerated(EnumType.STRING)
    private RefundStatus status;

    // PortOne에서 반환하는 환불 ID
    private String portOneRefundId;
    private String refundGroupId;
    private LocalDateTime refundedAt;

    @Builder
    private Refund(Payment payment, BigDecimal refundAmount, String reason, RefundStatus status, String portOneRefundId, String refundGroupId) {
        this.payment = payment;
        this.refundAmount = refundAmount;
        this.reason = reason;
        this.status = status != null ? status : RefundStatus.PENDING;
        this.portOneRefundId = portOneRefundId;
        this.refundGroupId = refundGroupId;
        this.refundedAt = LocalDateTime.now();
    }

    // 환불 완료 처리
    public void completeRefund(String portOneRefundId) {
        this.portOneRefundId = portOneRefundId;
        this.status = RefundStatus.COMPLETED;
        this.refundedAt = LocalDateTime.now();
    }

    // 환불 실패 처리
    public void failRefund() {
        this.status = RefundStatus.FAILED;
    }
}
