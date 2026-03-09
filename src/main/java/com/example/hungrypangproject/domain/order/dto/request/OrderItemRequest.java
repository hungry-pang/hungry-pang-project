package com.example.hungrypangproject.domain.order.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.Getter;

@Getter
@Data
public class OrderItemRequest {
    @NotEmpty
    private Long menuId;
    @NotEmpty
    private Long stock;
}
