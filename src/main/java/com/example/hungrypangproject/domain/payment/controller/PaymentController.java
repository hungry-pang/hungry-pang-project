package com.example.hungrypangproject.domain.payment.controller;

import com.example.hungrypangproject.domain.payment.dto.PaymentPrepareRequest;
import com.example.hungrypangproject.domain.payment.dto.PaymentPrepareResponse;
import com.example.hungrypangproject.domain.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제 준비 API
     * 클라이언트에서 결제를 시작하기 전에 호출하여 결제 정보를 생성
     */
    @PostMapping("/prepare")
    public ResponseEntity<PaymentPrepareResponse> preparePayment(
            @Valid @RequestBody PaymentPrepareRequest request) {

        log.info("결제 준비 요청 - orderId: {}, amount: {}", request.getOrderId(), request.getAmount());

        try {
            PaymentPrepareResponse response = paymentService.preparePayment(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("결제 준비 실패: {}", e.getMessage());
            throw e;
        }
    }
}
