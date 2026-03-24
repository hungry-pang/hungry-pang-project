package com.example.hungrypangproject.domain.order.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Getter;

@Getter
public class OrderItemRequest {
    @NotNull
    private Long menuId;

    @NotNull
    @Positive
    @JsonAlias("quantity")
    private Long stock;
}
