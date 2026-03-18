package com.example.hungrypangproject.domain.review.controller;

import com.example.hungrypangproject.domain.member.entity.MemberUserDetails;
import com.example.hungrypangproject.domain.review.dto.request.ReviewCreateRequest;
import com.example.hungrypangproject.domain.review.dto.request.ReviewStatusUpdateRequest;
import com.example.hungrypangproject.domain.review.dto.request.ReviewUpdateRequest;
import com.example.hungrypangproject.domain.review.dto.response.ReviewResponse;
import com.example.hungrypangproject.domain.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 작성
    // 주문을 완료한 사용자만 리뷰 작성 가능
    // 요청 시 Header에 memberId를 전달하여 주문자 본인인지 검증
    // memberId : 로그인한 사용자 ID
    @PostMapping("/orders/{orderId}/reviews")
    public ReviewResponse createReview(
            @PathVariable Long orderId,
            @RequestHeader("memberId") Long memberId,
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        return reviewService.createReview(orderId, memberId, request);
    }

    // 리뷰 목록 조회(식당별)
    @GetMapping("/stores/{storeId}/reviews")
    public List<ReviewResponse> getStoreReviews(@PathVariable Long storeId) {
        return reviewService.getStoreReviews(storeId);
    }

    // 리뷰 수정
    @PatchMapping("/reviews/{reviewId}")
    public ReviewResponse updateReview(
            @PathVariable Long reviewId,
            @RequestHeader("memberId") Long memberId,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        return reviewService.updateReview(reviewId, memberId, request);
    }

    // 리뷰 상태 변경
    //
    @PatchMapping("/reviews/{reviewId}/status")
    public ReviewResponse updateReviewStatus(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewStatusUpdateRequest request,
            @AuthenticationPrincipal MemberUserDetails userDetails
    ) {
        return reviewService.updateReviewStatus(reviewId, request, userDetails.getMember());
    }

    // 리뷰 삭제
    @DeleteMapping("/reviews/{reviewId}")
    public void deleteReview(
            @PathVariable Long reviewId,
            @RequestHeader("memberId") Long memberId
    ) {
        reviewService.deleteReview(reviewId, memberId);
    }

    // 리뷰 좋아요
    @PostMapping("/reviews/{reviewId}/like")
    public void likeReview(@PathVariable Long reviewId) {
        reviewService.likeReview(reviewId);
    }

    // 리뷰 좋아요 취소
    @DeleteMapping("/reviews/{reviewId}/like")
    public void unlikeReview(@PathVariable Long reviewId) {
        reviewService.unlikeReview(reviewId);
    }
}
