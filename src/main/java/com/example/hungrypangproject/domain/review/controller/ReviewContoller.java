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
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewContoller {

    private final ReviewService reviewService;

    /**
     * 리뷰 작성
     * - 특정 주문(orderId)에 대해 리뷰 작성
     * - 요청 헤더에서 로그인 사용자(memberId)를 받아 본인 주문 여부 검증
     */
    @PostMapping("/orders/{orderId}/reviews")
    public ReviewResponse createReview(
            @PathVariable Long orderId,
            @RequestHeader("memberId") Long memberId,
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        return reviewService.createReview(orderId, memberId, request);
    }

    /**
     * 식당별 리뷰 목록 조회
     * - 특정 식당(storeId)에 작성된 리뷰 목록 조회
     * - 서비스에서 EXPOSED 상태 리뷰만 반환
     */
    @GetMapping("/stores/{storeId}/reviews")
    public List<ReviewResponse> getStoreReviews(@PathVariable Long storeId) {
        return reviewService.getStoreReviews(storeId);
    }

    /**
     * 리뷰 수정
     * - reviewId에 해당하는 리뷰 수정
     * - 요청 헤더의 memberId로 작성자 본인 여부 검증
     */
    @PatchMapping("/reviews/{reviewId}")
    public ReviewResponse updateReview(
            @PathVariable Long reviewId,
            @RequestHeader("memberId") Long memberId,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        return reviewService.updateReview(reviewId, memberId, request);
    }

    /**
     * 리뷰 삭제
     * - reviewId에 해당하는 리뷰 삭제 (Soft Delete)
     * - 요청 헤더의 memberId로 작성자 본인 여부 검증
     */
    @DeleteMapping("/reviews/{reviewId}")
    public void deleteReview(
            @PathVariable Long reviewId,
            @RequestHeader("memberId") Long memberId
    ) {
        reviewService.deleteReview(reviewId, memberId);
    }

    /**
     * 리뷰 상태 변경 (관리자 기능)
     * - 리뷰 상태(EXPOSED, HIDDEN, DELETED) 변경
     */
    @PatchMapping("/reviews/{reviewId}/status")
    public ReviewResponse updateReviewStatus(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewStatusUpdateRequest request
    ) {
        return reviewService.updateReviewStatus(reviewId, request);
    }
}
