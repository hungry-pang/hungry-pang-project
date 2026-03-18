package com.example.hungrypangproject.domain.coupon.controller;

import com.example.hungrypangproject.common.dto.ApiResponse;
import com.example.hungrypangproject.domain.coupon.dto.CouponCreateRequest;
import com.example.hungrypangproject.domain.coupon.dto.CouponCreateResponse;
import com.example.hungrypangproject.domain.coupon.dto.CouponIssueResponse;
import com.example.hungrypangproject.domain.coupon.service.CouponService;
import com.example.hungrypangproject.domain.member.entity.MemberUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@Slf4j
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    public ApiResponse<CouponCreateResponse> createCoupon(
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @Valid @RequestBody CouponCreateRequest request
    ) {
        CouponCreateResponse response = couponService.createCoupon(userDetails.getMember().getRole(), request);
        return ApiResponse.created(response);
    }

    @PostMapping("/{couponId}/issue")
    public ApiResponse<CouponIssueResponse> issueCoupon(
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @PathVariable Long couponId
    ) {
        log.info("쿠폰 발급 요청 - couponId: {}, memberId: {}", couponId, userDetails.getMember().getMemberId());
        CouponIssueResponse response = couponService.issueCoupon(couponId);
        return ApiResponse.ok(response);
    }
}
