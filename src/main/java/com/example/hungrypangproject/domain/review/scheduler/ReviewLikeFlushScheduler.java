package com.example.hungrypangproject.domain.review.scheduler;

import com.example.hungrypangproject.domain.review.dto.internal.ReviewLikeDelta;
import com.example.hungrypangproject.domain.review.repository.ReviewBulkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewLikeFlushScheduler {

    private final StringRedisTemplate stringRedisTemplate;
    private final ReviewBulkRepository reviewBulkRepository;

    private static final String REVIEW_LIKE_KEY = "review:like:delta";

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void flushLikeCountToDb() {
        // 1. Redis 값 조회
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(REVIEW_LIKE_KEY);

        if (entries.isEmpty()) {
            log.info("[SCHEDULER] Redis에 반영할 좋아요 delta 없음");
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

        log.info("[SCHEDULER] DB 반영 대상 개수 = {}", deltas.size());

        // 3. 벌크 업데이트
        reviewBulkRepository.bulkUpdateLikeCount(deltas);

        // 4. Redis 초기화
        stringRedisTemplate.delete(REVIEW_LIKE_KEY);

        log.info("[SCHEDULER] Redis 좋아요 delta 삭제 완료");
    }
}
