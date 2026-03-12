package com.example.hungrypangproject.domain.order.dto.request;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class CreateOrderRequest {
    Long storeId;
    private List<OrderItemRequest> items;
    private String deliveryAddress;
    private BigDecimal usedPoint;
}
