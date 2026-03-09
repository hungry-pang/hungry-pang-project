package com.example.hungrypangproject.domain.review.repository;

import com.example.hungrypangproject.domain.review.entity.Review;
import com.example.hungrypangproject.domain.review.entity.ReviewStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByOrderId(Long orderId);

    @EntityGraph(attributePaths = {"store", "order", "member"})
    Optional<Review> findByIdAndStatusNot(Long reviewId, ReviewStatus status);

    @EntityGraph(attributePaths = {"store", "order", "member"})
    List<Review> findAllByStoreIdAndStatusNotOrderByCreatedAtDesc(Long storeId, ReviewStatus status);
}
