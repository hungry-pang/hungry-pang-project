package com.example.hungrypangproject.domain.review.entity;

import com.example.hungrypangproject.common.entity.BaseEntity;
import com.example.hungrypangproject.domain.member.entity.Member;
import com.example.hungrypangproject.domain.order.entity.Order;
import com.example.hungrypangproject.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "reviews")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "store_id")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id")
    private Member member;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false)
    private Integer rating;

    @Column(nullable = false, length = 200)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status;

    public static Review create(
            Store store,
            Order order,
            Member member,
            String name,
            Integer rating,
            String content
    ) {
        Review review = new Review();
        review.store = store;
        review.order = order;
        review.member = member;
        review.name = name;
        review.rating = rating;
        review.content = content;
        review.status = ReviewStatus.EXPOSED;
        return review;
    }

    // 리뷰 수정
    public void update(String content, Integer rating) {
        this.content = content;
        this.rating = rating;
    }

    // 리뷰 상태 변경
    public void updateStatus(ReviewStatus status) {
        this.status = status;
    }

    public void delete() {
        this.status = ReviewStatus.DELETED;
    }

    private static void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("별점은 1점 이상 5점 이하만 가능합니다.");
        }
    }

    private static void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("리뷰 내용은 필수입니다.");
        }
        if (content.length() > 200) {
            throw new IllegalArgumentException("리뷰 내용은 200자 이하만 가능합니다.");
        }
    }
}
