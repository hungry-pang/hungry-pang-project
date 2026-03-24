package com.example.hungrypangproject.domain.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentPrepareRequest {

    @NotNull(message = "주문 ID는 필수입니다.")
    private Long orderId;

    @NotNull(message = "결제 금액은 필수입니다.")
    @PositiveOrZero(message = "결제 금액은 0 이상이어야 합니다.")
    private BigDecimal amount;

    @PositiveOrZero(message = "사용 포인트는 0 이상이어야 합니다.")
    private BigDecimal pointsToUse;

    // 결제 방법 (card, trans, vbank 등)
    private String payMethod;

    // 구매자 정보
    private String buyerName;
    private String buyerTel;
    private String buyerEmail;
}
