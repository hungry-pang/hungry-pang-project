package com.example.hungrypangproject.domain.review.service;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.domain.member.entity.Member;
import com.example.hungrypangproject.domain.member.entity.MemberRoleEnum;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private final StringRedisTemplate stringRedisTemplate;

    private static final String REVIEW_LIKE_KEY = "review:like:delta";

    // 리뷰 작성
    @CacheEvict(value = "storeReviews", allEntries = true)
    public ReviewResponse createReview(Long orderId, Long loginMemberId, ReviewCreateRequest request) {

        // 주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ReviewException(ErrorCode.ORDER_NOT_FOUND));

        Member member = order.getMember();
        Long storeId = order.getStore().getId();

        // 비관적 락으로 식당 조회
        Store store = storeRepository.findByIdWithPessimisticLock(storeId)
                .orElseThrow(() -> new ReviewException(ErrorCode.STORE_NOT_FOUND));

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
                member.getNickname(),
                request.getRating(),
                request.getContent()
        );

        // 리뷰 저장
        Review savedReview = reviewRepository.save(review);

        // 식당 총 리뷰 수 증가
        store.increaseReviewCount();

        // DTO 변환 후 반환
        return ReviewResponse.from(savedReview);
    }

    // 리뷰 목록 조회(식당별)
    @Cacheable(value = "storeReviews", key = "#storeId")
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

    // 리뷰 수정
    @CacheEvict(value = "storeReviews", allEntries = true)
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

    // 리뷰 상태 변경
    @CacheEvict(value = "storeReviews", allEntries = true)
    public ReviewResponse updateReviewStatus(Long reviewId, ReviewStatusUpdateRequest request, Member loginMember) {

        // 관리자 권한 확인
        if (loginMember.getRole() != MemberRoleEnum.ROLE_ADMIN) {
            throw new ReviewException(ErrorCode.REVIEW_STATUS_FORBIDDEN);
        }

        // 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException(ErrorCode.REVIEW_NOT_FOUND));

        // 리뷰 상태 변경
        review.updateStatus(request.getStatus());

        return ReviewResponse.from(review);
    }

    // 리뷰 삭제
    @CacheEvict(value = "storeReviews", allEntries = true)
    public void deleteReview(Long reviewId, Long loginMemberId) {

        // 삭제되지 않은 리뷰 조회 (DELETED 상태 제외)
        Review review = reviewRepository.findByIdAndStatusNot(reviewId, ReviewStatus.DELETED)
                .orElseThrow(() -> new ReviewException(ErrorCode.REVIEW_NOT_FOUND));

        // 본인 리뷰인지 검증 (작성자만 삭제 가능)
        validateReviewOwner(review, loginMemberId);

        // 리뷰가 속한 식당 ID 조회
        Long storeId = review.getStore().getId();

        // 동시성 문제 방지를 위해 비관적 락으로 식당 조회
        // → 여러 트랜잭션이 동시에 리뷰 수를 수정하지 못하도록 row lock
        Store store = storeRepository.findByIdWithPessimisticLock(storeId)
                .orElseThrow(() -> new ReviewException(ErrorCode.STORE_NOT_FOUND));

        // 리뷰 soft delete 처리 (상태를 DELETED로 변경)
        review.delete();
        // 식당 총 리뷰 수 감소 (리뷰 삭제에 따른 집계 값 반영)
        store.decreaseReviewCount();
    }

    // 리뷰 좋아요 등록
    @CacheEvict(value = "storeReviews", allEntries = true)
    public void likeReview(Long reviewId) {
        validateReviewExists(reviewId);
        stringRedisTemplate.opsForHash()
                .increment(REVIEW_LIKE_KEY, String.valueOf(reviewId), 1);
    }

    // 리뷰 좋아요 삭제
    @CacheEvict(value = "storeReviews", allEntries = true)
    public void unlikeReview(Long reviewId) {
        validateReviewExists(reviewId);
        stringRedisTemplate.opsForHash()
                .increment(REVIEW_LIKE_KEY, String.valueOf(reviewId), -1);
    }

    // 리뷰 ID로 조회 없으면 REVIEW_NOT_FOUND 예외 발생
    private Review getReviewEntity(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException(ErrorCode.REVIEW_NOT_FOUND));
    }

    // 리뷰 작성자 검증
    private void validateReviewOwner(Review review, Long loginMemberId) {

        if (!review.getMember().getMemberId().equals(loginMemberId)) {
            throw new ReviewException(ErrorCode.REVIEW_FORBIDDEN);
        }
    }

    // 리뷰 좋아요 검증
    private void validateReviewExists(Long reviewId) {
        reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException(ErrorCode.REVIEW_NOT_FOUND));
    }
}
