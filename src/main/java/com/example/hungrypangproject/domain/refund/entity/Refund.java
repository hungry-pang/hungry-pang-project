package com.example.hungrypangproject.domain.refund.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
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

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "refund_amount", nullable = false)
    private BigDecimal refundAmount;

    @Column(nullable = false, length = 200)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private RefundStatus status;

    // PortOne에서 반환하는 환불 ID
    @Column(name = "portone_refund_id", length = 100)
    private String portOneRefundId;

    @Column(name = "refund_group_id", nullable = false, length = 100)
    private String refundGroupId;

    @Column(name = "refunded_at", nullable = false)
    private LocalDateTime refundedAt;

    // 환불 요청 이력 생성
    public static Refund createRequest(Long paymentId, BigDecimal refundAmount, String reason, String refundGroupId) {
        Refund refund = new Refund();
        refund.paymentId = paymentId;
        refund.refundAmount = refundAmount;
        refund.reason = reason;
        refund.refundGroupId = refundGroupId;
        refund.status = RefundStatus.PENDING;
        refund.refundedAt = LocalDateTime.now();
        return refund;
    }

    // 환불 완료 이력 생성
    public static Refund createCompleted(Long paymentId, BigDecimal refundAmount, String reason, String portOneRefundId, String refundGroupId) {
        Refund refund = new Refund();
        refund.paymentId = paymentId;
        refund.refundAmount = refundAmount;
        refund.reason = reason;
        refund.portOneRefundId = portOneRefundId;
        refund.refundGroupId = refundGroupId;
        refund.status = RefundStatus.COMPLETED;
        refund.refundedAt = LocalDateTime.now();
        return refund;
    }

    // 환불 실패 이력 생성
    public static Refund createFailed(Long paymentId, BigDecimal refundAmount, String reason, String portOneRefundId, String refundGroupId) {
        Refund refund = new Refund();
        refund.paymentId = paymentId;
        refund.refundAmount = refundAmount;
        refund.reason = reason;
        refund.portOneRefundId = portOneRefundId;
        refund.refundGroupId = refundGroupId;
        refund.status = RefundStatus.FAILED;
        refund.refundedAt = LocalDateTime.now();
        return refund;
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
