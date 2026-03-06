package com.example.hungrypangproject.domain.store.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class StoreUpdateRequest {

    private String storeName;
    private BigDecimal deliveryFee;
    private BigDecimal minimumOrder;
}
