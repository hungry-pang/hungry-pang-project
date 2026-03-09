package com.example.hungrypangproject.domain.review.service;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.domain.member.entity.Member;
import com.example.hungrypangproject.domain.order.entity.Order;
import com.example.hungrypangproject.domain.order.entity.OrderStatus;
import com.example.hungrypangproject.domain.order.repository.OrderRepository;
import com.example.hungrypangproject.domain.review.dto.request.ReviewCreateRequest;
import com.example.hungrypangproject.domain.review.dto.request.ReviewStatusUpdateRequest;
import com.example.hungrypangproject.domain.review.dto.request.ReviewUpdateRequest;
import com.example.hungrypangproject.domain.review.dto.response.ReviewResponse;
import com.example.hungrypangproject.domain.review.entity.Review;
import com.example.hungrypangproject.domain.review.entity.ReviewStatus;
import com.example.hungrypangproject.domain.review.exception.ReviewException;
import com.example.hungrypangproject.domain.review.repository.ReviewRepository;
import com.example.hungrypangproject.domain.store.entity.Store;
import com.example.hungrypangproject.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;

    /**
     * 리뷰 작성
     * - 주문 조회
     * - 본인 주문인지 확인
     * - 주문 완료 상태인지 확인
     * - 한 주문당 리뷰 1개 제한
     * - 리뷰 생성 및 저장
     */
    public ReviewResponse createReview(Long orderId, Long loginMemberId, ReviewCreateRequest request) {

        // 주문 조회 (없으면 ORDER_NOT_FOUND 예외)
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ReviewException(ErrorCode.ORDER_NOT_FOUND));

        Member member = order.getMember();
        Store store = order.getStore();

        // 본인 주문인지 확인
        if (!member.getMemberId().equals(loginMemberId)) {
            throw new ReviewException(ErrorCode.REVIEW_ORDER_FORBIDDEN);
        }

        // 주문 상태가 COMPLETED인지 확인 (주문 완료된 경우만 리뷰 작성 가능)
        if (order.getOrderStatus() != OrderStatus.COMPLETED) {
            throw new ReviewException(ErrorCode.REVIEW_ORDER_NOT_COMPLETED);
        }

        // 한 주문당 리뷰 1개만 작성 가능
        if (reviewRepository.existsByOrderId(orderId)) {
        throw new ReviewException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        // 리뷰 엔티티 생성
        Review review = Review.create(
                store,
                order,
                member,
                member.getNickname(), // 작성자 이름
                request.getRating(), // 별점
                request.getContent() // 리뷰 내용
        );

        // 리뷰 저장
        Review savedReview = reviewRepository.save(review);

        // DTO 변환 후 반환
        return ReviewResponse.from(savedReview);
    }

    /**
     * 식당별 리뷰 목록 조회
     * - 가게 존재 여부 확인
     * - 삭제되지 않은 리뷰 조회
     * - 노출 상태(EXPOSED) 리뷰만 반환
     */
    @Transactional(readOnly = true)
    public List<ReviewResponse> getStoreReviews(Long storeId) {

        // 가게 존재 여부 확인
        storeRepository.findById(storeId)
                .orElseThrow(() -> new ReviewException(ErrorCode.STORE_NOT_FOUND));

        // 리뷰 조회 (삭제되지 않은 리뷰)
        return reviewRepository.findAllByStoreIdAndStatusNotOrderByCreatedAtDesc(storeId, ReviewStatus.DELETED)
                .stream()
                // 노출 상태(EXPOSED) 리뷰만 필터링
                .filter(review -> review.getStatus() == ReviewStatus.EXPOSED)
                // DTO 변환
                .map(ReviewResponse::from)
                .toList();
    }

    /**
     * 리뷰 수정
     * - 리뷰 조회
     * - 본인 리뷰인지 확인
     * - 리뷰 내용 및 별점 수정
     */
    public ReviewResponse updateReview(Long reviewId, Long loginMemberId, ReviewUpdateRequest request) {

        // 삭제되지 않은 리뷰 조회
        Review review = reviewRepository.findByIdAndStatusNot(reviewId, ReviewStatus.DELETED)
                .orElseThrow(() -> new ReviewException(ErrorCode.REVIEW_NOT_FOUND));

        // 본인 리뷰인지 검증
        validateReviewOwner(review, loginMemberId);

        // 리뷰 내용 및 별점 수정
        review.update(request.getContent(), request.getRating());

        return ReviewResponse.from(review);
    }

    /**
     * 리뷰 삭제 (Soft Delete)
     * - 리뷰 조회
     * - 본인 리뷰인지 확인
     * - 상태를 DELETED로 변경
     */
    public void deleteReview(Long reviewId, Long loginMemberId) {

        // 삭제되지 않은 리뷰 조회
        Review review = reviewRepository.findByIdAndStatusNot(reviewId, ReviewStatus.DELETED)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        // 본인 리뷰인지 검증
        validateReviewOwner(review, loginMemberId);

        // soft delete 처리
        review.delete();
    }

    /**
     * 리뷰 상태 변경 (관리자 기능)
     * - 리뷰 조회
     * - 리뷰 상태 변경 (EXPOSED / HIDDEN / DELETED)
     */
    public ReviewResponse updateReviewStatus(Long reviewId, ReviewStatusUpdateRequest request) {

        // 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException(ErrorCode.REVIEW_NOT_FOUND));

        // 리뷰 상태 변경
        review.updateStatus(request.getStatus());

        return ReviewResponse.from(review);
    }

    /**
     * 리뷰 작성자 검증
     * - 로그인 사용자와 리뷰 작성자가 같은지 확인
     */
    private void validateReviewOwner(Review review, Long loginMemberId) {

        if (!review.getMember().getMemberId().equals(loginMemberId)) {
            throw new ReviewException(ErrorCode.REVIEW_FORBIDDEN);
        }
    }
}
