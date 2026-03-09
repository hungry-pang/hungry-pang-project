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
    private final String orderNum;
    private final BigDecimal totalPrice;
    private final OrderStatus status;
    private final LocalDateTime orderedAt;
    private final List<String> menuNames;


    private OrderListResponse(Long id, String orderNum, BigDecimal totalPrice, OrderStatus status, LocalDateTime orderedAt, List<String> menuNames) {
        this.id = id;
        this.orderNum = orderNum;
        this.totalPrice = totalPrice;
        this.status = status;
        this.orderedAt = orderedAt;
        this.menuNames = menuNames;
    }

    public static OrderListResponse from(Order order, List<OrderItem> orderItems) {
        List<String> menuNames = orderItems.stream()
                .map(OrderItem::getName)
                .toList();
        return new OrderListResponse(
                order.getId(),
                order.getOrderNum().toString(),
                order.getTotalPrice(),
                order.getOrderStatus(),
                order.getOrderAt(),
                menuNames
        );
    }

}
