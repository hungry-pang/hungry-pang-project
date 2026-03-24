package com.example.hungrypangproject.domain.order.dto.request;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;

@Getter
public class OrderItemRequest {
    @NotNull
    private Long menuId;
    @NotNull
    private Long stock;
}
