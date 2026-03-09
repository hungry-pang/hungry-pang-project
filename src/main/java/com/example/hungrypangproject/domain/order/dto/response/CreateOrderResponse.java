package com.example.hungrypangproject.domain.order.dto.response;

import com.example.hungrypangproject.domain.order.entity.Order;
import com.example.hungrypangproject.domain.order.entity.OrderItem;
import com.example.hungrypangproject.domain.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Getter
public class CreateOrderResponse {
    private final Long id;
    private final String orderNum;
    private final BigDecimal totalPrice;
    private final OrderStatus status;
    private final LocalDateTime orderedAt;
    private final BigDecimal usedPoint;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
    private final List<OrderItemResponse> items;

    public CreateOrderResponse(Long id, String orderNum, BigDecimal totalPrice, OrderStatus status, LocalDateTime orderedAt, BigDecimal usedPoint, LocalDateTime createdAt, LocalDateTime modifiedAt,  List<OrderItemResponse> items) {
        this.id = id;
        this.orderNum = orderNum;
        this.totalPrice = totalPrice;
        this.status = status;
        this.orderedAt = orderedAt;
        this.usedPoint = usedPoint;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.items = items;
    }

    // from() 추가
    public static CreateOrderResponse from(Order order, List<OrderItem> orderItems) {
        List<OrderItemResponse> items = orderItems.stream()//orderItems를 Dto 리스트로 변환
                .map(OrderItemResponse::from)
                .toList();
        return new CreateOrderResponse(
                order.getId(),
                order.getOrderNum().toString(),
                order.getTotalPrice(),
                order.getOrderStatus(),
                order.getOrderAt(),
                order.getUsedPoint(),
                order.getCreatedAt(),
                order.getModifiedAt(),
                items
        );
    }

    @Getter
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long menuId;
        private Long orderId;
        private String menuName;
        private BigDecimal price;
        private Long stock;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;

        public static OrderItemResponse from(OrderItem orderItem) {
            return new OrderItemResponse(
                    orderItem.getMenu().getId(),
                    orderItem.getOrder().getId(),
                    orderItem.getName(),
                    orderItem.getPrice(),
                    orderItem.getStock(),
                    orderItem.getCreatedAt(),
                    orderItem.getModifiedAt()
            );
        }
    }
}
