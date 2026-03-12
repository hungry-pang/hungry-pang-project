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

    private Long paymentId;

    private BigDecimal refundAmount;

    private String reason;

    @Enumerated(EnumType.STRING)
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
    // 환불 요청 이력 생성
    public static Refund createRequest(Long paymentId, BigDecimal refundAmount, String reason, String refundGroupId) {
        return Refund.builder()
                .paymentId(paymentId)
                .refundAmount(refundAmount)
                .reason(reason)
                .refundGroupId(refundGroupId)
                .status(RefundStatus.PENDING)
                .build();
    }

    // 환불 완료 이력 생성
    public static Refund createCompleted(Long paymentId, BigDecimal refundAmount, String reason, String portOneRefundId, String refundGroupId) {
        return Refund.builder()
                .paymentId(paymentId)
                .refundAmount(refundAmount)
                .reason(reason)
                .portOneRefundId(portOneRefundId)
                .refundGroupId(refundGroupId)
                .status(RefundStatus.COMPLETED)
                .build();
    }

    // 환불 실패 이력 생성
    public static Refund createFailed(Long paymentId, BigDecimal refundAmount, String reason, String portOneRefundId, String refundGroupId) {
        return Refund.builder()
                .paymentId(paymentId)
                .refundAmount(refundAmount)
                .reason(reason)
                .portOneRefundId(portOneRefundId)
                .refundGroupId(refundGroupId)
                .status(RefundStatus.FAILED)
                .build();
    }

    // 환불 실패 처리
    public void failRefund() {
        this.status = RefundStatus.FAILED;
    }
}
