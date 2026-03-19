package com.example.hungrypangproject.domain.store.entity;

import com.example.hungrypangproject.common.entity.BaseEntity;
import com.example.hungrypangproject.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "stores")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "member_id")
    private Member seller;

    public static Store create(
            String storeName,
            BigDecimal deliveryFee,
            BigDecimal minimumOrder,
            Member seller
    ) {
        Store store = new Store();
        store.storeName = storeName;
        store.deliveryFee = deliveryFee;
        store.status = StoreStatus.OPEN;
        store.minimumOrder = minimumOrder;
        store.seller = seller;
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

    public boolean isOwner(Long memberId) {
        return this.seller.getMemberId().equals(memberId);
    }
}
