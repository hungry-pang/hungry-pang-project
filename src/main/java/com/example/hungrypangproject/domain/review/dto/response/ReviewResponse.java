package com.example.hungrypangproject.domain.review.dto.response;

import com.example.hungrypangproject.domain.review.entity.Review;
import com.example.hungrypangproject.domain.review.entity.ReviewStatus;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewResponse implements Serializable {

    private Long reviewId;
    private Long storeId;
    private Long orderId;
    private Long memberId;
    private String writerName;
    private Integer rating;
    private String content;
    private ReviewStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .reviewId(review.getId())
                .storeId(review.getStore().getId())
                .orderId(review.getOrder().getId())
                .memberId(review.getMember().getMemberId())
                .writerName(review.getName())
                .rating(review.getRating())
                .content(review.getContent())
                .status(review.getStatus())
                .createdAt(review.getCreatedAt())
                .modifiedAt(review.getModifiedAt())
                .build();
    }
}
