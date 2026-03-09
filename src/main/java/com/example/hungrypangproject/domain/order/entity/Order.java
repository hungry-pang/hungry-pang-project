package com.example.hungrypangproject.domain.order.entity;

import com.example.hungrypangproject.common.entity.BaseEntity;
import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.domain.member.entity.Member;
import com.example.hungrypangproject.domain.order.exception.OrderException;
import com.example.hungrypangproject.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @UuidGenerator
    @Column(name = "order_num", unique = true, nullable = false)
    private UUID orderNum;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    @Column(name = "order_at", nullable = false)
    private LocalDateTime orderAt;

    @Column(name = "used_point")
    private BigDecimal usedPoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "store_id")
    private Store store;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    public static Order create(
            BigDecimal totalPrice,
            BigDecimal usedPoint,
            Member member,
            Store store
    ) {
        Order order = new Order();
        order.totalPrice = totalPrice;
        order.orderStatus = OrderStatus.WATING; // 초기 상태값
        order.orderAt = LocalDateTime.now();
        order.usedPoint = usedPoint;
        order.member = member;
        order.store = store;
        return order;
    }

    public void cancel(Long userId) {
        if(!this.member.getMemberId().equals(userId)){
            throw new OrderException(ErrorCode.ORDER_CANCEL_FORBIDDEN);
        }
        if(this.orderStatus != OrderStatus.WATING){
            throw new OrderException(ErrorCode.ORDER_NOT_CANCELABLE);
        }
        this.orderStatus = OrderStatus.REFUNDED;
    }

}
