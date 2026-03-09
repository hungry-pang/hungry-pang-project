package com.example.hungrypangproject.domain.review.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewCreateRequest {

    private Integer rating;
    private String content;
}
