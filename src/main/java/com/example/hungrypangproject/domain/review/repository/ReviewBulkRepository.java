package com.example.hungrypangproject.domain.review.repository;

import com.example.hungrypangproject.domain.review.dto.internal.ReviewLikeDelta;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReviewBulkRepository {

    private final JdbcTemplate jdbcTemplate;

    public void bulkUpdateLikeCount(List<ReviewLikeDelta> deltas) {
        String sql = """
            UPDATE reviews
            SET like_count = like_count + ?
            WHERE id = ?
        """;

        jdbcTemplate.batchUpdate(
                sql,
                deltas,
                deltas.size(),
                (ps, delta) -> {
                    ps.setLong(1, delta.getDelta());
                    ps.setLong(2, delta.getReviewId());
                }
        );
    }
}
