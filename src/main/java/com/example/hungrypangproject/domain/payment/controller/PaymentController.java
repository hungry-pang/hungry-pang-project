package com.example.hungrypangproject.domain.payment.controller;

import com.example.hungrypangproject.domain.payment.dto.PaymentPrepareRequest;
import com.example.hungrypangproject.domain.payment.dto.PaymentPrepareResponse;
import com.example.hungrypangproject.domain.payment.dto.PaymentVerifyRequest;
import com.example.hungrypangproject.domain.payment.dto.PaymentVerifyResponse;
import com.example.hungrypangproject.domain.payment.dto.WebhookRequest;
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

        PaymentPrepareResponse response = paymentService.preparePayment(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 결제 검증 API
     * 클라이언트에서 PortOne 결제 완료 후 호출하여 결제를 검증하고 주문 상태를 변경
     */
    @PostMapping("/verify")
    public ResponseEntity<PaymentVerifyResponse> verifyPayment(
            @Valid @RequestBody PaymentVerifyRequest request) {

        log.info("결제 검증 요청 - impUid: {}, merchantUid: {}", request.getImpUid(), request.getMerchantUid());

        PaymentVerifyResponse response = paymentService.verifyPayment(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 웹훅 처리 API
     * PortOne에서 결제 상태 변경 시 자동으로 호출하는 엔드포인트
     *
     * 주의사항:
     * - 이 API는 PortOne 서버에서만 호출되어야 합니다
     * - 프로덕션 환경에서는 PortOne IP 화이트리스트 검증 추가 권장
     * - 멱등성이 보장되어 중복 호출 시에도 안전합니다
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody WebhookRequest request) {

        log.info("웹훅 수신 - impUid: {}, merchantUid: {}, status: {}",
                request.getImp_uid(), request.getMerchant_uid(), request.getStatus());

        String result = paymentService.processWebhook(request);
        return ResponseEntity.ok(result);
    }
}
