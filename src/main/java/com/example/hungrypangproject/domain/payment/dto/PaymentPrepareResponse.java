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
public class PaymentPrepareResponse {

    // 서버에서 생성한 결제 ID (merchant_uid)
    private String dbPaymentId;

    // 주문 정보
    private Long orderId;
    private String orderName;

    // 결제 금액 정보
    private BigDecimal amount;
    private BigDecimal pointsToUse;
    private BigDecimal finalAmount;

    // 구매자 정보
    private String buyerName;
    private String buyerTel;
    private String buyerEmail;

    // PortOne 결제 요청에 필요한 정보
    private String payMethod;
}
