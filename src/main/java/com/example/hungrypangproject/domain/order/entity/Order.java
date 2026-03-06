package com.example.hungrypangproject.domain.order.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @UuidGenerator
    @Column(name = "order_num", unique = true, nullable = false)
    private UUID orderNum;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    @Column(name = "order_at", nullable = false)
    private LocalDateTime orderAt;

    @Column(name = "used_point")
    private BigDecimal usedPoint;

}
