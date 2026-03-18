package com.example.hungrypangproject.common.scheduler;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.domain.review.entity.Review;
import com.example.hungrypangproject.domain.review.exception.ReviewException;
import com.example.hungrypangproject.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ReviewLikeFlushScheduler {

    private final StringRedisTemplate stringRedisTemplate;
    private final ReviewRepository reviewRepository;

    private static final String REVIEW_LIKE_KEY = "review:like:delta";

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void flushLikeCountToDb() {
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(REVIEW_LIKE_KEY);

        if (entries.isEmpty()) {
            return;
        }

        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            Long reviewId = Long.parseLong(entry.getKey().toString());
            Long delta = Long.parseLong(entry.getValue().toString());

            if (delta == 0L) {
                continue;
            }

            Review review = reviewRepository.findById(reviewId)
                    .orElseThrow(() -> new ReviewException(ErrorCode.REVIEW_NOT_FOUND));

            review.applyLikeDelta(delta);
        }

        stringRedisTemplate.delete(REVIEW_LIKE_KEY);
    }
}
