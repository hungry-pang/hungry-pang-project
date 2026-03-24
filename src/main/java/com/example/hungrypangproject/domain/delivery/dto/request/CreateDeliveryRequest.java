package com.example.hungrypangproject.domain.delivery.dto.request;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CreateDeliveryRequest {
    private Long orderId;
    private String deliveryAddress;
    private BigDecimal deliveryFee;

}
