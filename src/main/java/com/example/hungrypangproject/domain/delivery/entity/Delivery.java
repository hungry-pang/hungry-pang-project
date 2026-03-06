package com.example.hungrypangproject.domain.delivery.entity;

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
    private LocalDateTime deliveryAddress;

    @Column(name = "delivery_fee", nullable = false)
    private BigDecimal deliveryFee;

    private DeliveryStatus deliveryStatus;

    @Column(name = "pick_up_at", nullable = false)
    private LocalDateTime pickupAt;
    @Column(name = "delivery_at")
    private LocalDateTime deliveryAt;

}
