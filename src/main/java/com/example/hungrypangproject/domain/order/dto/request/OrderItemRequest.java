package com.example.hungrypangproject.domain.order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;

@Getter
@Data
public class OrderItemRequest {
    @NotNull
    private Long menuId;
    @NotNull
    private Long stock;
}
