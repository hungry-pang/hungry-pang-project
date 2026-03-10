package com.example.hungrypangproject.domain.order.dto.response;

import com.example.hungrypangproject.domain.order.entity.Order;
import com.example.hungrypangproject.domain.order.entity.OrderItem;
import com.example.hungrypangproject.domain.order.entity.OrderStatus;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class OrderDetailResponse {
    private final Long id;
    private final String orderNum;
    private final BigDecimal totalPrice;
    private final OrderStatus status;
    private final LocalDateTime orderedAt;
    private final List<MenuSummary> menus;


    private OrderDetailResponse(Long id, String orderNum, BigDecimal totalPrice, OrderStatus status, LocalDateTime orderedAt, List<MenuSummary> menus) {
        this.id = id;
        this.orderNum = orderNum;
        this.totalPrice = totalPrice;
        this.status = status;
        this.orderedAt = orderedAt;
        this.menus = menus;
    }

    public static OrderDetailResponse from(Order order, List<OrderItem> orderItems) {
        List<MenuSummary> menus = orderItems.stream()
                .map(MenuSummary::from)
                .toList();
        return new OrderDetailResponse(
                order.getId(),
                order.getOrderNum().toString(),
                order.getTotalPrice(),
                order.getOrderStatus(),
                order.getOrderAt(),
                menus
        );
    }

    @Getter
    public static class MenuSummary {
        private final String menuName;
        private final Long stock;

        private MenuSummary(String menuName, Long stock) {
            this.menuName = menuName;
            this.stock = stock;
        }

        public static MenuSummary from(OrderItem orderItem) {
            return new MenuSummary(orderItem.getName(), orderItem.getStock());
        }
    }
}
