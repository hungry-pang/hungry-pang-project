package com.example.hungrypangproject.domain.order.dto.response;

import com.example.hungrypangproject.domain.order.entity.Order;
import com.example.hungrypangproject.domain.order.entity.OrderItem;
import com.example.hungrypangproject.domain.order.entity.OrderStatus;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class OrderListResponse {
    private final Long id;
    private final String storeName;
    private final String menus;
    private final BigDecimal totalPrice;
    private final OrderStatus orderStatus;
    private final LocalDateTime orderedAt;

    private OrderListResponse(Long id, String storeName, String menus, BigDecimal totalPrice, OrderStatus orderStatus, LocalDateTime orderedAt) {
        this.id = id;
        this.storeName = storeName;
        this.menus = menus;
        this.totalPrice = totalPrice;
        this.orderStatus = orderStatus;
        this.orderedAt = orderedAt;
    }
    public static OrderListResponse from(Order order) {
        List<OrderItem> orderItems = order.getOrderItems();
        if (orderItems.isEmpty()) {
            return new OrderListResponse(
                    order.getId(),
                    order.getStore().getStoreName(),
                    "주문 항목 없음",
                    order.getTotalPrice(),
                    order.getOrderStatus(),
                    order.getOrderAt()
            );
        }
        String menus = orderItems.get(0).getName();
        if (orderItems.size() > 1) {
            menus += " 외 " + (orderItems.size() - 1) + "건";
        }
        return new OrderListResponse(
                order.getId(),
                order.getStore().getStoreName(),
                menus,
                order.getTotalPrice(),
                order.getOrderStatus(),
                order.getOrderAt()
        );
    }
}
