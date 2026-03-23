package com.example.hungrypangproject.domain.payment.entity;

import com.example.hungrypangproject.domain.order.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 서버에서 생성한 ID (결제 요청 시 사용)
    @Column(name = "db_payment_id", nullable = false, unique = true, length = 100)
    private String dbPaymentId;

    // PortOne에서 발급받은 영수증 ID (결제 완료 후 기록)
    @Column(name = "payment_id", unique = true, length = 100)
    private String paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "points_to_use", nullable = false)
    private BigDecimal pointsToUse;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private PaymentStatus status;

    // 결제 준비 시 생성
    public static Payment create(String dbPaymentId, Order order, BigDecimal totalAmount, BigDecimal pointsToUse) {
        Payment payment = new Payment();
        payment.dbPaymentId = dbPaymentId;
        payment.order = order;
        payment.totalAmount = totalAmount;
        payment.pointsToUse = pointsToUse;
        payment.status = PaymentStatus.PENDING;
        return payment;
    }

    // 상태 변경 메서드
    public void updateStatus(PaymentStatus status) {
        this.status = status;
    }

    // 결제 검증 시작
    public void startVerification() {
        this.status = PaymentStatus.VERIFYING;
    }

    public void completePayment(String paymentId) {
        this.paymentId = paymentId;
        this.status = PaymentStatus.PAID;
    }

    public void failPayment() {
        this.status = PaymentStatus.FAIL;
    }

    // 일시적 장애 시 결제 대기 상태로 복구
    public void restorePending() {
        this.status = PaymentStatus.PENDING;
    }

    // 환불 처리 시작
    public void startRefund() {
        this.status = PaymentStatus.REFUNDING;
    }

    public void refund() {
        this.status = PaymentStatus.REFUND;
    }

    // 환불 실패 시 결제 완료 상태로 복구
    public void restorePaid() {
        this.status = PaymentStatus.PAID;
    }

    // 환불 시 결제 상태 검증
    public boolean isRefund() {
        return this.status == PaymentStatus.REFUND;
    }

    public boolean isRefunding() {
        return this.status == PaymentStatus.REFUNDING;
    }

    public boolean isVerifying() {
        return this.status == PaymentStatus.VERIFYING;
    }

    public boolean isPaid() {
        return this.status == PaymentStatus.PAID;
    }

    // 동시성 제어용 상태 전이 메서드
    public void startVerification() {
        this.status = PaymentStatus.VERIFYING;
    }
    public boolean isVerifying() {
        return this.status == PaymentStatus.VERIFYING;
    }
    public void startRefund() {
        this.status = PaymentStatus.REFUNDING;
    }
    public boolean isRefunding() {
        return this.status == PaymentStatus.REFUNDING;
    }
}
