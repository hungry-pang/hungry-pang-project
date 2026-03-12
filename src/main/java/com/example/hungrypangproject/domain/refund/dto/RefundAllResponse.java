package com.example.hungrypangproject.domain.refund.dto;

import lombok.Getter;

import java.util.UUID;

@Getter
public class RefundAllResponse {

    private final Long orderId;
    private final UUID orderNum;

    public RefundAllResponse(Long orderId, UUID orderNum) {
        this.orderId = orderId;
        this.orderNum = orderNum;
    }
}
