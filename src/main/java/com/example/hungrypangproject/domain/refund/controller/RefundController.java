package com.example.hungrypangproject.domain.refund.controller;

import com.example.hungrypangproject.common.dto.ApiResponse;
import com.example.hungrypangproject.domain.member.entity.MemberUserDetails;
import com.example.hungrypangproject.domain.refund.dto.RefundAllRequest;
import com.example.hungrypangproject.domain.refund.dto.RefundAllResponse;
import com.example.hungrypangproject.domain.refund.dto.RefundDetailResponse;
import com.example.hungrypangproject.domain.refund.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
@Slf4j
public class RefundController {

    private final RefundService refundService;

    @GetMapping("/{refundId}")
    public ResponseEntity<ApiResponse<RefundDetailResponse>> getRefundDetail(
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @PathVariable Long refundId
    ) {
        Long memberId = userDetails.getMember().getMemberId();
        RefundDetailResponse response = refundService.getRefundDetail(memberId, refundId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{dbPaymentId}")
    public ResponseEntity<ApiResponse<RefundAllResponse>> refundAll(
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @PathVariable String dbPaymentId,
            @Valid @RequestBody RefundAllRequest refundAllRequest
    ) {
        log.info("환불 요청 - orderId: {}, reason: {}", refundAllRequest.getOrderId(), refundAllRequest.getReason());

        Long memberId = userDetails.getMember().getMemberId();
        RefundAllResponse response = refundService.refundAll(memberId, dbPaymentId, refundAllRequest);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
