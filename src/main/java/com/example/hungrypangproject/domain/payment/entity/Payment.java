package com.example.hungrypangproject.domain.payment.entity;

import com.example.hungrypangproject.domain.order.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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
    private String dbPaymentId;

    // PortOne에서 발급받은 영수증 ID (결제 완료 후 기록)
    private String paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    private BigDecimal totalAmount;

    private BigDecimal pointsToUse;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Builder
    public Payment(String dbPaymentId, String paymentId, Order order, BigDecimal totalAmount, BigDecimal pointsToUse) {
        this.dbPaymentId = dbPaymentId;
        this.paymentId = paymentId;
        this.order = order;
        this.totalAmount = totalAmount;
        this.pointsToUse = pointsToUse;
        this.status = getStatus();
    }
}
