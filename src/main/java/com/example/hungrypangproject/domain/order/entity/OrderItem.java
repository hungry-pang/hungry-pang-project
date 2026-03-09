package com.example.hungrypangproject.domain.order.entity;

import com.example.hungrypangproject.common.entity.BaseEntity;
import com.example.hungrypangproject.domain.menu.entity.Menu;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "order_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;        // 주문 시점 메뉴명 스냅샷

    @Column(nullable = false)
    private BigDecimal price;   // 주문 시점 가격 스냅샷

    @Column(nullable = false)
    private Long stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "menu_id")
    private Menu menu;

    public static OrderItem create(Order order, Menu menu, Long stock) {
        OrderItem orderItem = new OrderItem();
        orderItem.order = order;
        orderItem.menu = menu;
        orderItem.name = menu.getName();
        orderItem.price = menu.getPrice();
        orderItem.stock = stock;
        return orderItem;
    }
}