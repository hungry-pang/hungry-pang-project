package com.example.hungrypangproject.domain.review.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewLikeDelta {

    private Long reviewId;
    private Long delta;
}
