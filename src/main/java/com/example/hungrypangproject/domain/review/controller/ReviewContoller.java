package com.example.hungrypangproject.domain.review.controller;

import com.example.hungrypangproject.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewContoller {

    private final ReviewService reviewService;
}
