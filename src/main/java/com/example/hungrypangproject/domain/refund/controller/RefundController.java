package com.example.hungrypangproject.domain.refund.controller;

import com.example.hungrypangproject.domain.refund.dto.RefundAllRequest;
import com.example.hungrypangproject.domain.refund.dto.RefundAllResponse;
import com.example.hungrypangproject.domain.refund.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
@Slf4j
public class RefundController {

    private final RefundService refundService;

    @PostMapping("/{dbPaymentId}")
    public ResponseEntity<RefundAllResponse> refundAll(
            @PathVariable String dbPaymentId,
            @Valid @RequestBody RefundAllRequest refundAllRequest
    ) {
        log.info("환불 요청 - orderId: {}, reason: {}", refundAllRequest.getOrderId(), refundAllRequest.getReason());

        RefundAllResponse response = refundService.refundAll(dbPaymentId, refundAllRequest);
        return ResponseEntity.ok(response);
    }
}
