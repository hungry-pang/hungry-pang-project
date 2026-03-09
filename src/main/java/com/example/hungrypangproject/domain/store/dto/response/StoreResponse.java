package com.example.hungrypangproject.domain.store.dto.response;

import com.example.hungrypangproject.domain.store.entity.Store;
import com.example.hungrypangproject.domain.store.entity.StoreStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PROTECTED)
public class StoreResponse {

    private Long id;
    private String storeName;
    private BigDecimal deliveryFee;
    private StoreStatus status;
    private BigDecimal minimumOrder;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static StoreResponse from(Store store) {
        return StoreResponse.builder()
                .id(store.getId())
                .storeName(store.getStoreName())
                .deliveryFee(store.getDeliveryFee())
                .status(store.getStatus())
                .minimumOrder(store.getMinimumOrder())
                .createdAt(store.getCreatedAt())
                .modifiedAt(store.getModifiedAt())
                .build();
    }
}
