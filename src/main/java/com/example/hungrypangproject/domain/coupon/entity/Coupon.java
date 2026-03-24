package com.example.hungrypangproject.domain.coupon.entity;

import com.example.hungrypangproject.domain.coupon.exception.CouponException;
import com.example.hungrypangproject.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "coupons")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coupon_name", nullable = false)
    private String couponName;

    @Column(name = "discount_amount", nullable = false)
    private BigDecimal discountAmount;

    @Column(name = "total_quantity", nullable = false)
    private int totalQuantity;

    @Column(name = "issued_quantity", nullable = false)
    private int issuedQuantity;

    public static Coupon create(String couponName, BigDecimal discountAmount, int totalQuantity) {
        if (couponName == null || couponName.isBlank()) {
            throw new CouponException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (discountAmount == null || discountAmount.signum() < 0) {
            throw new CouponException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (totalQuantity <= 0) {
            throw new CouponException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Coupon coupon = new Coupon();
        coupon.couponName = couponName;
        coupon.discountAmount = discountAmount;
        coupon.totalQuantity = totalQuantity;
        coupon.issuedQuantity = 0;
        return coupon;
    }

    public void issueCoupon() {
        if (this.issuedQuantity >= this.totalQuantity) {
            throw new CouponException(ErrorCode.COUPON_QUANTITY_EXCEEDED);
        }
        this.issuedQuantity++;
    }

    public int getRemainingQuantity() {
        return this.totalQuantity - this.issuedQuantity;
    }
}
