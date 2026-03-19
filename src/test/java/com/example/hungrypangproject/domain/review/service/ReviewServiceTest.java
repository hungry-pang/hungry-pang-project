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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testcontainers.shaded.org.bouncycastle.asn1.x500.style.RFC4519Style.member;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @InjectMocks
    private ReviewService reviewService;

    private Member member;
    private Member adminMember;
    private Store store;
    private Order order;

    @BeforeEach
    void setUp() {
        member = mock(Member.class);
        adminMember = mock(Member.class);
        store = mock(Store.class);
        order = mock(Order.class);
    }

    @Test
    @DisplayName("리뷰 작성 성공")
    void createReview_Success() {
        // given
        ReviewCreateRequest request = new ReviewCreateRequest();
        ReflectionTestUtils.setField(request, "rating", 5);
        ReflectionTestUtils.setField(request, "content", "정말 맛있어요!");

        when(member.getMemberId()).thenReturn(1L);
        when(member.getNickname()).thenReturn("리뷰유저");

        when(store.getId()).thenReturn(1L);

        when(order.getId()).thenReturn(1L);
        when(order.getMember()).thenReturn(member);
        when(order.getStore()).thenReturn(store);
        when(order.getOrderStatus()).thenReturn(OrderStatus.COMPLETED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(storeRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(store));
        when(reviewRepository.existsByOrderId(1L)).thenReturn(false);

        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review savedReview = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedReview, "id", 1L);
            return savedReview;
        });

        // when
        ReviewResponse response = reviewService.createReview(1L, 1L, request);

        // then
        assertNotNull(response);
        assertEquals(1L, response.getReviewId());
        assertEquals(1L, response.getStoreId());
        assertEquals(1L, response.getOrderId());
        assertEquals(1L, response.getMemberId());
        assertEquals(5, response.getRating());
        assertEquals("정말 맛있어요!", response.getContent());

        verify(orderRepository, times(1)).findById(1L);
        verify(storeRepository, times(1)).findByIdWithPessimisticLock(1L);
        verify(reviewRepository, times(1)).existsByOrderId(1L);
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(store, times(1)).increaseReviewCount();
    }

    @Test
    @DisplayName("리뷰 작성 실패 - 주문이 존재하지 않음")
    void createReview_Fail_OrderNotFound() {
        // given
        ReviewCreateRequest request = new ReviewCreateRequest();
        ReflectionTestUtils.setField(request, "rating", 5);
        ReflectionTestUtils.setField(request, "content", "정말 맛있어요!");

        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        ReviewException exception = assertThrows(ReviewException.class,
                () -> reviewService.createReview(1L, 1L, request));

        // then
        assertEquals(ErrorCode.ORDER_NOT_FOUND.getMessage(), exception.getMessage());

        verify(orderRepository, times(1)).findById(1L);
        verify(storeRepository, never()).findByIdWithPessimisticLock(anyLong());
        verify(reviewRepository, never()).existsByOrderId(anyLong());
        verify(reviewRepository, never()).save(any(Review.class));
        verify(store, never()).increaseReviewCount();
    }

    @Test
    @DisplayName("리뷰 작성 실패 - 주문 상태가 완료가 아님")
    void createReview_Fail_OrderNotCompleted() {
        // given
        ReviewCreateRequest request = new ReviewCreateRequest();
        ReflectionTestUtils.setField(request, "rating", 5);
        ReflectionTestUtils.setField(request, "content", "정말 맛있어요!");

        when(member.getMemberId()).thenReturn(1L);
        when(store.getId()).thenReturn(1L);

        when(order.getMember()).thenReturn(member);
        when(order.getStore()).thenReturn(store);
        when(order.getOrderStatus()).thenReturn(OrderStatus.PREPARING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(storeRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(store));

        // when
        ReviewException exception = assertThrows(ReviewException.class,
                () -> reviewService.createReview(1L, 1L, request));

        // then
        assertEquals(ErrorCode.REVIEW_ORDER_NOT_COMPLETED.getMessage(), exception.getMessage());

        verify(orderRepository, times(1)).findById(1L);
        verify(storeRepository, times(1)).findByIdWithPessimisticLock(1L);
        verify(reviewRepository, never()).existsByOrderId(anyLong());
        verify(reviewRepository, never()).save(any(Review.class));
        verify(store, never()).increaseReviewCount();
    }

    @Test
    @DisplayName("리뷰 작성 실패 - 본인 주문이 아님")
    void createReview_Fail_OrderForbidden() {
        // given
        ReviewCreateRequest request = new ReviewCreateRequest();
        ReflectionTestUtils.setField(request, "rating", 5);
        ReflectionTestUtils.setField(request, "content", "정말 맛있어요!");

        when(member.getMemberId()).thenReturn(2L);
        when(store.getId()).thenReturn(1L);

        when(order.getMember()).thenReturn(member);
        when(order.getStore()).thenReturn(store);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(storeRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(store));

        // when
        ReviewException exception = assertThrows(ReviewException.class,
                () -> reviewService.createReview(1L, 1L, request));

        // then
        assertEquals(ErrorCode.REVIEW_ORDER_FORBIDDEN.getMessage(), exception.getMessage());

        verify(orderRepository, times(1)).findById(1L);
        verify(storeRepository, times(1)).findByIdWithPessimisticLock(1L);
        verify(reviewRepository, never()).existsByOrderId(anyLong());
        verify(reviewRepository, never()).save(any(Review.class));
        verify(store, never()).increaseReviewCount();
    }

    @Test
    @DisplayName("리뷰 작성 실패 - 이미 리뷰가 존재함")
    void createReview_Fail_AlreadyExists() {
        // given
        ReviewCreateRequest request = new ReviewCreateRequest();
        ReflectionTestUtils.setField(request, "rating", 5);
        ReflectionTestUtils.setField(request, "content", "정말 맛있어요!");

        when(member.getMemberId()).thenReturn(1L);
        when(store.getId()).thenReturn(1L);

        when(order.getMember()).thenReturn(member);
        when(order.getStore()).thenReturn(store);
        when(order.getOrderStatus()).thenReturn(OrderStatus.COMPLETED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(storeRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(store));
        when(reviewRepository.existsByOrderId(1L)).thenReturn(true);

        // when
        ReviewException exception = assertThrows(ReviewException.class,
                () -> reviewService.createReview(1L, 1L, request));

        // then
        assertEquals(ErrorCode.REVIEW_ALREADY_EXISTS.getMessage(), exception.getMessage());

        verify(orderRepository, times(1)).findById(1L);
        verify(storeRepository, times(1)).findByIdWithPessimisticLock(1L);
        verify(reviewRepository, times(1)).existsByOrderId(1L);
        verify(reviewRepository, never()).save(any(Review.class));
        verify(store, never()).increaseReviewCount();
    }

    @Test
    @DisplayName("식당 리뷰 목록 조회 성공")
    void getStoreReviews_Success() {
        // given
        Review review1 = mock(Review.class);
        Review review2 = mock(Review.class);

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(reviewRepository.findAllByStoreIdAndStatusNotOrderByCreatedAtDesc(1L, ReviewStatus.DELETED))
                .thenReturn(List.of(review1, review2));

        when(review1.getStatus()).thenReturn(ReviewStatus.EXPOSED);
        when(review1.getId()).thenReturn(1L);
        when(review1.getStore()).thenReturn(store);
        when(review1.getOrder()).thenReturn(order);
        when(review1.getMember()).thenReturn(member);
        when(review1.getName()).thenReturn("리뷰유저");
        when(review1.getRating()).thenReturn(5);
        when(review1.getContent()).thenReturn("맛있어요");
        when(review1.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(review1.getModifiedAt()).thenReturn(LocalDateTime.now());

        when(review2.getStatus()).thenReturn(ReviewStatus.HIDDEN);

        when(store.getId()).thenReturn(1L);
        when(order.getId()).thenReturn(1L);
        when(member.getMemberId()).thenReturn(1L);

        // when
        List<ReviewResponse> responses = reviewService.getStoreReviews(1L);

        // then
        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).getReviewId());
        assertEquals("맛있어요", responses.get(0).getContent());

        verify(storeRepository, times(1)).findById(1L);
        verify(reviewRepository, times(1))
                .findAllByStoreIdAndStatusNotOrderByCreatedAtDesc(1L, ReviewStatus.DELETED);
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 리뷰가 존재하지 않음")
    void updateReview_Fail_ReviewNotFound() {
        // given
        ReviewUpdateRequest request = new ReviewUpdateRequest();
        ReflectionTestUtils.setField(request, "rating", 4);
        ReflectionTestUtils.setField(request, "content", "수정된 리뷰");

        when(reviewRepository.findByIdAndStatusNot(1L, ReviewStatus.DELETED))
                .thenReturn(Optional.empty());

        // when
        ReviewException exception = assertThrows(ReviewException.class,
                () -> reviewService.updateReview(1L, 1L, request));

        // then
        assertEquals(ErrorCode.REVIEW_NOT_FOUND.getMessage(), exception.getMessage());

        verify(reviewRepository, times(1)).findByIdAndStatusNot(1L, ReviewStatus.DELETED);
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 본인 리뷰가 아님")
    void updateReview_Fail_Forbidden() {
        // given
        Review review = mock(Review.class);
        ReviewUpdateRequest request = new ReviewUpdateRequest();
        ReflectionTestUtils.setField(request, "rating", 4);
        ReflectionTestUtils.setField(request, "content", "수정된 리뷰");

        when(reviewRepository.findByIdAndStatusNot(1L, ReviewStatus.DELETED))
                .thenReturn(Optional.of(review));

        when(review.getMember()).thenReturn(member);
        when(member.getMemberId()).thenReturn(2L);

        // when
        ReviewException exception = assertThrows(ReviewException.class,
                () -> reviewService.updateReview(1L, 1L, request));

        // then
        assertEquals(ErrorCode.REVIEW_FORBIDDEN.getMessage(), exception.getMessage());

        verify(reviewRepository, times(1)).findByIdAndStatusNot(1L, ReviewStatus.DELETED);
        verify(review, never()).update(anyString(), anyInt());
    }

    @Test
    @DisplayName("리뷰 상태 변경 성공 - 관리자")
    void updateReviewStatus_Success() {
        // given
        Review review = mock(Review.class);
        ReviewStatusUpdateRequest request = new ReviewStatusUpdateRequest();
        ReflectionTestUtils.setField(request, "status", ReviewStatus.HIDDEN);

        when(adminMember.getRole()).thenReturn(MemberRoleEnum.ROLE_ADMIN);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        when(review.getId()).thenReturn(1L);
        when(review.getStore()).thenReturn(store);
        when(review.getOrder()).thenReturn(order);
        when(review.getMember()).thenReturn(member);
        when(review.getName()).thenReturn("리뷰유저");
        when(review.getRating()).thenReturn(5);
        when(review.getContent()).thenReturn("정말 맛있어요!");
        when(review.getStatus()).thenReturn(ReviewStatus.HIDDEN);
        when(review.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(review.getModifiedAt()).thenReturn(LocalDateTime.now());

        when(store.getId()).thenReturn(1L);
        when(order.getId()).thenReturn(1L);
        when(member.getMemberId()).thenReturn(1L);

        // when
        ReviewResponse response = reviewService.updateReviewStatus(1L, request, adminMember);

        // then
        assertNotNull(response);
        assertEquals(ReviewStatus.HIDDEN, response.getStatus());

        verify(reviewRepository, times(1)).findById(1L);
        verify(review, times(1)).updateStatus(ReviewStatus.HIDDEN);
    }

    @Test
    @DisplayName("리뷰 상태 변경 실패 - 관리자가 아님")
    void updateReviewStatus_Fail_NotAdmin() {
        // given
        ReviewStatusUpdateRequest request = new ReviewStatusUpdateRequest();
        ReflectionTestUtils.setField(request, "status", ReviewStatus.HIDDEN);

        when(member.getRole()).thenReturn(MemberRoleEnum.ROLE_USER);

        // when
        ReviewException exception = assertThrows(ReviewException.class,
                () -> reviewService.updateReviewStatus(1L, request, member));

        // then
        assertEquals(ErrorCode.REVIEW_STATUS_FORBIDDEN.getMessage(), exception.getMessage());

        verify(reviewRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("리뷰 상태 변경 실패 - 리뷰가 존재하지 않음")
    void updateReviewStatus_Fail_ReviewNotFound() {
        // given
        ReviewStatusUpdateRequest request = new ReviewStatusUpdateRequest();
        ReflectionTestUtils.setField(request, "status", ReviewStatus.HIDDEN);

        when(adminMember.getRole()).thenReturn(MemberRoleEnum.ROLE_ADMIN);
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        ReviewException exception = assertThrows(ReviewException.class,
                () -> reviewService.updateReviewStatus(1L, request, adminMember));

        // then
        assertEquals(ErrorCode.REVIEW_NOT_FOUND.getMessage(), exception.getMessage());

        verify(reviewRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("리뷰 삭제 성공")
    void deleteReview_Success() {
        // given
        Review review = mock(Review.class);

        when(reviewRepository.findByIdAndStatusNot(1L, ReviewStatus.DELETED))
                .thenReturn(Optional.of(review));

        when(review.getMember()).thenReturn(member);
        when(member.getMemberId()).thenReturn(1L);

        when(review.getStore()).thenReturn(store);
        when(store.getId()).thenReturn(1L);
        when(storeRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(store));

        // when
        reviewService.deleteReview(1L, 1L);

        // then
        verify(reviewRepository, times(1)).findByIdAndStatusNot(1L, ReviewStatus.DELETED);
        verify(storeRepository, times(1)).findByIdWithPessimisticLock(1L);
        verify(review, times(1)).delete();
        verify(store, times(1)).decreaseReviewCount();
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 리뷰가 존재하지 않음")
    void deleteReview_Fail_ReviewNotFound() {
        // given
        when(reviewRepository.findByIdAndStatusNot(1L, ReviewStatus.DELETED))
                .thenReturn(Optional.empty());

        // when
        ReviewException exception = assertThrows(ReviewException.class,
                () -> reviewService.deleteReview(1L, 1L));

        // then
        assertEquals(ErrorCode.REVIEW_NOT_FOUND.getMessage(), exception.getMessage());

        verify(reviewRepository, times(1)).findByIdAndStatusNot(1L, ReviewStatus.DELETED);
        verify(storeRepository, never()).findByIdWithPessimisticLock(anyLong());
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 본인 리뷰가 아님")
    void deleteReview_Fail_Forbidden() {
        // given
        Review review = mock(Review.class);

        when(reviewRepository.findByIdAndStatusNot(1L, ReviewStatus.DELETED))
                .thenReturn(Optional.of(review));

        when(review.getMember()).thenReturn(member);
        when(member.getMemberId()).thenReturn(2L);

        // when
        ReviewException exception = assertThrows(ReviewException.class,
                () -> reviewService.deleteReview(1L, 1L));

        // then
        assertEquals(ErrorCode.REVIEW_FORBIDDEN.getMessage(), exception.getMessage());

        verify(reviewRepository, times(1)).findByIdAndStatusNot(1L, ReviewStatus.DELETED);
        verify(storeRepository, never()).findByIdWithPessimisticLock(anyLong());
        verify(review, never()).delete();
    }

    @Test
    @DisplayName("리뷰 좋아요 등록 성공")
    void likeReview_Success() {
        // given
        Review review = mock(Review.class);

        when(reviewRepository.findById(1L))
                .thenReturn(Optional.of(review));
        when(stringRedisTemplate.opsForHash())
                .thenReturn(hashOperations);
        when(hashOperations.increment("review:like:delta", "1", 1))
                .thenReturn(1L);

        // when
        reviewService.likeReview(1L);

        // then
        verify(reviewRepository, times(1)).findById(1L);
        verify(stringRedisTemplate, times(1)).opsForHash();
        verify(hashOperations, times(1))
                .increment("review:like:delta", "1", 1);
    }

    @Test
    @DisplayName("리뷰 좋아요 등록 실패 - 리뷰가 존재하지 않음")
    void likeReview_Fail_ReviewNotFound() {
        // given
        when(reviewRepository.findById(1L))
                .thenReturn(Optional.empty());

        // when
        ReviewException exception = assertThrows(ReviewException.class,
                () -> reviewService.likeReview(1L));

        // then
        assertEquals(ErrorCode.REVIEW_NOT_FOUND.getMessage(), exception.getMessage());
        verify(reviewRepository, times(1)).findById(1L);
        verify(stringRedisTemplate, never()).opsForHash();
    }

    @Test
    @DisplayName("리뷰 좋아요 삭제 성공")
    void unlikeReview_Success() {
        // given
        Review review = mock(Review.class);

        when(reviewRepository.findById(1L))
                .thenReturn(Optional.of(review));
        when(stringRedisTemplate.opsForHash())
                .thenReturn(hashOperations);
        when(hashOperations.increment("review:like:delta", "1", -1))
                .thenReturn(0L);

        // when
        reviewService.unlikeReview(1L);

        // then
        verify(reviewRepository, times(1)).findById(1L);
        verify(stringRedisTemplate, times(1)).opsForHash();
        verify(hashOperations, times(1))
                .increment("review:like:delta", "1", -1);
    }

    @Test
    @DisplayName("리뷰 좋아요 삭제 실패 - 리뷰가 존재하지 않음")
    void unlikeReview_Fail_ReviewNotFound() {
        // given
        when(reviewRepository.findById(1L))
                .thenReturn(Optional.empty());

        // when
        ReviewException exception = assertThrows(ReviewException.class,
                () -> reviewService.unlikeReview(1L));

        // then
        assertEquals(ErrorCode.REVIEW_NOT_FOUND.getMessage(), exception.getMessage());
        verify(reviewRepository, times(1)).findById(1L);
        verify(stringRedisTemplate, never()).opsForHash();
    }
}
