package com.example.hungrypangproject.domain.review.dto.request;

import com.example.hungrypangproject.domain.review.entity.ReviewStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewStatusUpdateRequest {

    private ReviewStatus status;
}
