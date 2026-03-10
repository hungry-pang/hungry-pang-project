package com.example.hungrypangproject.domain.delivery.dto.response;

import com.example.hungrypangproject.domain.delivery.entity.Delivery;
import com.example.hungrypangproject.domain.delivery.entity.DeliveryStatus;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class DeliveryDetailResponse {
    private final Long deliveryId;
    private final Long orderId;
    private final String deliveryAddress;
    private final BigDecimal deliveryFee;
    private final DeliveryStatus deliveryStatus;
    private final Long riderId;
    private final LocalDateTime pickupAt;
    private final LocalDateTime deliveryAt;

    private DeliveryDetailResponse(Long deliveryId, Long orderId, String deliveryAddress, BigDecimal deliveryFee, DeliveryStatus deliveryStatus, Long riderId, LocalDateTime pickupAt, LocalDateTime deliveryAt) {
        this.deliveryId = deliveryId;
        this.orderId = orderId;
        this.deliveryAddress = deliveryAddress;
        this.deliveryFee = deliveryFee;
        this.deliveryStatus = deliveryStatus;
        this.riderId = riderId;
        this.pickupAt = pickupAt;
        this.deliveryAt = deliveryAt;
    }

    public static DeliveryDetailResponse from(Delivery delivery) {
        return new DeliveryDetailResponse(
                delivery.getId(),
                delivery.getOrder().getId(),
                delivery.getDeliveryAddress(),
                delivery.getDeliveryFee(),
                delivery.getDeliveryStatus(),
                delivery.getRiderId(),
                delivery.getPickupAt(),
                delivery.getDeliveryAt()
        );
    }
}
