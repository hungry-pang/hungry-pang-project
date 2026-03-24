package com.example.hungrypangproject.domain.payment.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVerifyRequest {

    /**
     * PortOne v2 결제 ID
     */
    @NotBlank(message = "paymentId는 필수입니다")
    @JsonAlias({"impUid", "imp_uid"})
    private String paymentId;

    /**
     * 우리 서버에서 생성한 결제 ID
     */
    @NotBlank(message = "dbPaymentId는 필수입니다")
    @JsonAlias({"merchantUid", "merchant_uid"})
    private String dbPaymentId;

    /**
     * PortOne v2 트랜잭션 ID (선택값)
     */
    private String txId;
}
