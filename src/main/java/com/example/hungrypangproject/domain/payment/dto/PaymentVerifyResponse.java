package com.example.hungrypangproject.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVerifyResponse {

    private boolean success;
    private String message;
    private Long paymentId;
    private Long orderId;
    private String impUid;
    private BigDecimal paidAmount;
    private String orderStatus;

    public static PaymentVerifyResponse success(Long paymentId, Long orderId, String impUid, BigDecimal paidAmount, String orderStatus) {
        return PaymentVerifyResponse.builder()
                .success(true)
                .message("결제 검증 성공")
                .paymentId(paymentId)
                .orderId(orderId)
                .impUid(impUid)
                .paidAmount(paidAmount)
                .orderStatus(orderStatus)
                .build();
    }

    public static PaymentVerifyResponse alreadyProcessed() {
        return PaymentVerifyResponse.builder()
                .success(true)
                .message("이미 처리된 결제입니다")
                .build();
    }
}
