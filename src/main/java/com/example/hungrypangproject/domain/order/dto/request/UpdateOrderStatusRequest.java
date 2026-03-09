package com.example.hungrypangproject.domain.order.dto.request;

import com.example.hungrypangproject.domain.order.entity.OrderStatus;
import lombok.Getter;

@Getter
public class UpdateOrderStatusRequest {
    private OrderStatus orderStatus;
}
