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

    @JoinColumn(name = "store_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Store store;

    @JoinColumn(name = "order_id", nullable = false, unique = true)
    @OneToOne(fetch = FetchType.LAZY)
    private Order order;

    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
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

    public Review(Store store, Order order, Member member, String name, Integer rating, String content, ReviewStatus status) {
        this.store = store;
        this.order = order;
        this.member = member;
        this.name = name;
        this.rating = rating;
        this.content = content;
        this.status = status;
    }

    public void update(String content, Integer rating) {
        this.content = content;
        this.rating = rating;
    }

    public void updateStatus(ReviewStatus status) {
        this.status = status;
    }
}
