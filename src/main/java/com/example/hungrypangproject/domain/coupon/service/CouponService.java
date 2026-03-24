package com.example.hungrypangproject.domain.coupon.service;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.common.lock.RedisLock;
import com.example.hungrypangproject.domain.coupon.dto.CouponCreateRequest;
import com.example.hungrypangproject.domain.coupon.dto.CouponCreateResponse;
import com.example.hungrypangproject.domain.coupon.dto.CouponIssueResponse;
import com.example.hungrypangproject.domain.coupon.entity.Coupon;
import com.example.hungrypangproject.domain.coupon.exception.CouponException;
import com.example.hungrypangproject.domain.coupon.repository.CouponRepository;
import com.example.hungrypangproject.domain.member.entity.MemberRoleEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {

    private final CouponRepository couponRepository;

    @Transactional
    public CouponCreateResponse createCoupon(MemberRoleEnum role, CouponCreateRequest request) {
        validateAdmin(role);

        Coupon coupon = Coupon.create(
                request.getCouponName(),
                request.getDiscountAmount(),
                request.getTotalQuantity()
        );
        Coupon savedCoupon = couponRepository.save(coupon);

        return new CouponCreateResponse(
                savedCoupon.getId(),
                savedCoupon.getCouponName(),
                savedCoupon.getDiscountAmount(),
                savedCoupon.getTotalQuantity()
        );
    }

    @Transactional
    @RedisLock(keyPrefix = "coupon:issue:", argIndex = 0, ttlSeconds = 3)
    public CouponIssueResponse issueCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponException(ErrorCode.COUPON_NOT_FOUND));

        coupon.issueCoupon();

        return new CouponIssueResponse(
                coupon.getId(),
                coupon.getIssuedQuantity(),
                coupon.getRemainingQuantity()
        );
    }

    private void validateAdmin(MemberRoleEnum role) {
        if (role != MemberRoleEnum.ROLE_ADMIN) {
            throw new CouponException(ErrorCode.COUPON_FORBIDDEN);
        }
    }
}
