package com.example.hungrypangproject.domain.delivery.entity;

import com.example.hungrypangproject.domain.order.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "deliverys")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rider_id", unique = true)
    private Long riderId;

    @Column(name = "delivery_address", nullable = false)
    private String deliveryAddress;

    @Column(name = "delivery_fee", nullable = false)
    private BigDecimal deliveryFee;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;

    @Column(name = "pick_up_at", nullable = false)
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
            LocalDateTime pickupAt,
            Order order
    ) {
        Delivery delivery = new Delivery();
        delivery.riderId = riderId;
        delivery.deliveryAddress = deliveryAddress;
        delivery.deliveryFee = deliveryFee;
        delivery.deliveryStatus = DeliveryStatus.PICKEDUP;
        delivery.pickupAt = pickupAt;
        delivery.deliveryAt = null;
        delivery.order = order;
        return delivery;
    }


}
