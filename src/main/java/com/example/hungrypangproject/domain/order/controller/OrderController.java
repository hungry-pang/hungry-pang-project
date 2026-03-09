package com.example.hungrypangproject.domain.order.controller;

import com.example.hungrypangproject.common.dto.ApiResponse;
import com.example.hungrypangproject.domain.order.dto.request.CreateOrderRequest;
import com.example.hungrypangproject.domain.order.dto.response.CreateOrderResponse;
import com.example.hungrypangproject.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ApiResponse<CreateOrderResponse> createOrder(
            @RequestBody CreateOrderRequest request,
            @RequestParam Long userId
    ) {
        CreateOrderResponse response = orderService.save(userId, request);
        return ApiResponse.created(response);
    }

    @PatchMapping("/{orderId}/cancel")
    public ApiResponse<Void>  cancelOrder(
            @PathVariable Long orderId,
            @RequestParam Long userId
    ){
        orderService.cancelOrder(orderId, userId);
        return ApiResponse.ok();
    }
}
