package com.example.hungrypangproject.domain.payment.dto;

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
     * PortOne에서 발급한 결제 고유 ID
     */
    @NotBlank(message = "imp_uid는 필수입니다")
    private String impUid;

    /**
     * 우리 서버에서 생성한 결제 ID (merchant_uid)
     */
    @NotBlank(message = "merchant_uid는 필수입니다")
    private String merchantUid;
}
