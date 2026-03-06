package com.example.hungrypangproject.domain.store.entity;

import com.example.hungrypangproject.common.entity.BaseEntity;
import com.example.hungrypangproject.domain.menu.entity.Menu;
import com.example.hungrypangproject.domain.order.entity.Order;
import com.example.hungrypangproject.domain.review.entity.Review;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "stores")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_name", nullable = false, length = 10)
    private String storeName;

    @Column(name = "delivery_fee")
    private BigDecimal deliveryFee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoreStatus status;

    @Column(name = "minimum_order")
    private BigDecimal minimumOrder;

    public static Store create(
            String storeName,
            BigDecimal deliveryFee,
            BigDecimal minimumOrder
    ) {
        Store store = new Store();
        store.storeName = storeName;
        store.deliveryFee = deliveryFee;
        store.status = StoreStatus.OPEN;
        store.minimumOrder = minimumOrder;
        return store;
    }

    public void update(String storeName, BigDecimal deliveryFee, BigDecimal minimumOrder) {
        this.storeName = storeName;
        this.deliveryFee = deliveryFee;
        this.minimumOrder = minimumOrder;
    }

    public void updateStatus(StoreStatus status) {
        this.status = status;
    }
}
