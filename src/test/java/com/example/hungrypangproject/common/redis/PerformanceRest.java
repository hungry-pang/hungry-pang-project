package com.example.hungrypangproject.common.redis;

import com.example.hungrypangproject.common.security.RedisUtil;
import com.example.hungrypangproject.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class PerformanceRest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RedisUtil redisUtil;

    @Test
    @DisplayName("RefreshToken DB vs. Redis 조회 성능 비교")
    void comparerPerformance() {
        String testToken = "test-token-value-12345678";
        String testEmail = "test@test.com";

        // DB 블랙리스트 조회 성능 측정
        long dbStartTime = System.nanoTime();
        memberRepository.findByEmail(testEmail);

        long dbEndTime = System.nanoTime();
        long dbDuration = dbEndTime - dbStartTime;

        // Redis 블랙리스트 조회 성능 측정
        redisUtil.setBlackList(testToken, "logout", Duration.ofMinutes(10));

        long redisStartTime = System.nanoTime();
        redisUtil.isBlackList(testToken);

        long redisEndTime = System.nanoTime();
        long redisDuration = redisEndTime - redisStartTime;

        System.out.println("DB Query Time: " + dbDuration + "ns");
        System.out.println("Redis Query Time: " + redisDuration + "ns");

        assertThat(redisDuration).isLessThan(dbDuration);
    }

}
