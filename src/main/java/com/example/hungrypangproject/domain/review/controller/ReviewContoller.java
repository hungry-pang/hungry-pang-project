package com.example.hungrypangproject.domain.review.controller;

import com.example.hungrypangproject.domain.review.dto.request.ReviewCreateRequest;
import com.example.hungrypangproject.domain.review.dto.request.ReviewStatusUpdateRequest;
import com.example.hungrypangproject.domain.review.dto.request.ReviewUpdateRequest;
import com.example.hungrypangproject.domain.review.dto.response.ReviewResponse;
import com.example.hungrypangproject.domain.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewContoller {

    private final ReviewService reviewService;

    // 리뷰 작성
    @PostMapping("/orders/{orderId}/reviews")
    public ReviewResponse createReview(
            @PathVariable Long orderId,
            @RequestHeader("memberId") Long memberId,
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        return reviewService.createReview(orderId, memberId, request);
    }

    // 식당별 리뷰 목록 조회
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

    // 리뷰 삭제
    @DeleteMapping("/reviews/{reviewId}")
    public void deleteReview(
            @PathVariable Long reviewId,
            @RequestHeader("memberId") Long memberId
    ) {
        reviewService.deleteReview(reviewId, memberId);
    }

    // 리뷰 상태 변경 (관리자)
    @PatchMapping("/reviews/{reviewId}/status")
    public ReviewResponse updateReviewStatus(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewStatusUpdateRequest request
    ) {
        return reviewService.updateReviewStatus(reviewId, request);
    }
}
