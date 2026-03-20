package com.example.hungrypangproject.domain.review.scheduler;

import com.example.hungrypangproject.domain.review.dto.internal.ReviewLikeDelta;
import com.example.hungrypangproject.domain.review.repository.ReviewBulkRepository;
import com.example.hungrypangproject.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ReviewLikeFlushScheduler {

    private final StringRedisTemplate stringRedisTemplate;
    private final ReviewBulkRepository reviewBulkRepository;

    private static final String REVIEW_LIKE_KEY = "review:like:delta";

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void flushLikeCountToDb() {
        // 1. Redis 값 조회
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(REVIEW_LIKE_KEY);

        if (entries.isEmpty()) {
            return;
        }

        // 2. 벌크 처리용 리스트 생성
        List<ReviewLikeDelta> deltas = new ArrayList<>();

        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            Long reviewId = Long.parseLong(entry.getKey().toString());
            Long delta = Long.parseLong(entry.getValue().toString());

            if (delta == 0L) continue;

            deltas.add(new ReviewLikeDelta(reviewId, delta));
        }

        // 3. 벌크 업데이트
        reviewBulkRepository.bulkUpdateLikeCount(deltas);

        // 4. Redis 초기화
        stringRedisTemplate.delete(REVIEW_LIKE_KEY);
    }
}
