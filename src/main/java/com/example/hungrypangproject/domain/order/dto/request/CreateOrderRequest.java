package com.example.hungrypangproject.domain.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class CreateOrderRequest {
    @NotNull
    private Long storeId;

    @Valid
    @NotEmpty
    private List<OrderItemRequest> items;

    @NotBlank
    private String deliveryAddress;

    private BigDecimal usedPoint;
}
