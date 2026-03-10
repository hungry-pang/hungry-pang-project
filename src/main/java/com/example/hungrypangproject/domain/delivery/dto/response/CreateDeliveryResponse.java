package com.example.hungrypangproject.domain.delivery.dto.response;

import com.example.hungrypangproject.domain.delivery.entity.Delivery;
import com.example.hungrypangproject.domain.delivery.entity.DeliveryStatus;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class CreateDeliveryResponse {

    private final Long deliveryId;
    private final Long orderId;
    private final String deliveryAddress;
    private final BigDecimal deliveryFee;
    private final DeliveryStatus deliveryStatus;
    private final String raiderNickname;
    private final LocalDateTime pickupAt;

    private CreateDeliveryResponse(Long deliveryId, Long orderId, String deliveryAddress, BigDecimal deliveryFee, DeliveryStatus deliveryStatus, String raiderNickname, LocalDateTime pickupAt) {
        this.deliveryId = deliveryId;
        this.orderId = orderId;
        this.deliveryAddress = deliveryAddress;
        this.deliveryFee = deliveryFee;
        this.deliveryStatus = deliveryStatus;
        this.raiderNickname = raiderNickname;
        this.pickupAt = pickupAt;
    }

    public static CreateDeliveryResponse from(Delivery delivery, String raiderNickname) {
        return new CreateDeliveryResponse(
                delivery.getId(),
                delivery.getOrder().getId(),
                delivery.getDeliveryAddress(),
                delivery.getDeliveryFee(),
                delivery.getDeliveryStatus(),
                raiderNickname,
                delivery.getPickupAt()
        );
    }
}
