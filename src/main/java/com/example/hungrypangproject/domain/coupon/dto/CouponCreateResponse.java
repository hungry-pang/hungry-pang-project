package com.example.hungrypangproject.domain.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class CouponCreateResponse {

	private Long couponId;
	private String couponName;
	private BigDecimal discountAmount;
	private int totalQuantity;
}

