package com.example.hungrypangproject.domain.delivery.controller;

import com.example.hungrypangproject.common.dto.ApiResponse;
import com.example.hungrypangproject.domain.delivery.dto.request.CreateDeliveryRequest;
import com.example.hungrypangproject.domain.delivery.dto.response.CreateDeliveryResponse;
import com.example.hungrypangproject.domain.delivery.dto.response.DeliveryDetailResponse;
import com.example.hungrypangproject.domain.delivery.repository.DeliveryRepository;
import com.example.hungrypangproject.domain.delivery.service.DeliveryService;
import com.example.hungrypangproject.domain.member.entity.MemberUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/deliverys")
public class DeliveryController {
    private final DeliveryService deliveryService;


    // 배달 요청 생성 - 셀러만 가능
    @Secured("ROLE_SELLER")
    @PostMapping
    public ApiResponse<CreateDeliveryResponse> createDelivery(
            @RequestBody CreateDeliveryRequest request
    ) {
        return ApiResponse.created(deliveryService.createDelivery(request));
    }

    // 배달 완료 처리 - 라이더만 가능
    @Secured("ROLE_RAIDER")
    @PatchMapping("/{deliveryId}/complete")
    public ApiResponse<Void> completeDelivery(
            @PathVariable Long deliveryId,
            @AuthenticationPrincipal MemberUserDetails userDetails
    ) {
        Long riderId = userDetails.getMember().getMemberId();
        deliveryService.completeDelivery(deliveryId, riderId);
        return ApiResponse.ok();
    }

    // 내 주문 배달 상태 조회 - 유저
    @GetMapping("/orders/{orderId}")
    public ApiResponse<DeliveryDetailResponse> getDeliveryByOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal MemberUserDetails userDetails
    ) {
        Long userId = userDetails.getMember().getMemberId();
        return ApiResponse.ok(deliveryService.getDeliveryByOrder(orderId, userId));
    }
}
