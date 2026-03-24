package com.example.hungrypangproject.domain.delivery.entity;

import com.example.hungrypangproject.common.entity.BaseEntity;
import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.domain.delivery.exception.DeliveryException;
import com.example.hungrypangproject.domain.order.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "deliveries")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rider_id", nullable = false)
    private Long riderId;

    @Column(name = "delivery_address", nullable = false)
    private String deliveryAddress;

    @Column(name = "delivery_fee", nullable = false)
    private BigDecimal deliveryFee;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;

    @Column(name = "pick_up_at")
    private LocalDateTime pickupAt;
    @Column(name = "delivery_at")
    private LocalDateTime deliveryAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "order_id")
    private Order order;

    public static Delivery create(
            Long riderId,
            String deliveryAddress,
            BigDecimal deliveryFee,
            Order order
    ) {
        Delivery delivery = new Delivery();
        delivery.riderId = riderId;
        delivery.deliveryAddress = deliveryAddress;
        delivery.deliveryFee = deliveryFee;
        delivery.deliveryStatus = DeliveryStatus.PENDING;
        delivery.deliveryAt = null;
        delivery.order = order;
        return delivery;
    }

    // 시스템이 라이더 배정
    public void startDelivering() {
        if (this.deliveryStatus != DeliveryStatus.PENDING) {
            throw new DeliveryException(ErrorCode.DELIVERY_NOT_CHANGEABLE);
        }
        this.deliveryStatus = DeliveryStatus.DELIVERING;
        this.pickupAt = LocalDateTime.now();
    }

    // 라이더가 배달 완료
    public void complete(Long riderId) {
        if (!this.riderId.equals(riderId)) {
            throw new DeliveryException(ErrorCode.DELIVERY_FORBIDDEN);
        }
        if (this.deliveryStatus != DeliveryStatus.DELIVERING) {
            throw new DeliveryException(ErrorCode.DELIVERY_NOT_CHANGEABLE);
        }
        this.deliveryStatus = DeliveryStatus.COMPLETED;
        this.deliveryAt = LocalDateTime.now();
    }


}
