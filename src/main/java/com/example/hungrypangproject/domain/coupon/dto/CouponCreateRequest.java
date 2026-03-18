package com.example.hungrypangproject.domain.coupon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CouponCreateRequest {

    @NotBlank(message = "쿠폰 이름은 필수입니다.")
    private String couponName;

    @NotNull(message = "할인 금액은 필수입니다.")
    @PositiveOrZero(message = "할인 금액은 0 이상이어야 합니다.")
    private BigDecimal discountAmount;

    @Positive(message = "총 발급 수량은 1 이상이어야 합니다.")
    private int totalQuantity;
}

