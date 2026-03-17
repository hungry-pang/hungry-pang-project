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
public class BlackListPerformanceTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RedisUtil redisUtil;

    @Test
    @DisplayName("RefreshToken DB vs. Redis 조회 성능 비교 - 50,000번 반복")
    void comparerPerformance() {
        String testToken = "test-token-value-12345678";
        String testEmail = "test@test.com";
        int iterations = 50000;

        // Redis 데이터 셋팅
        redisUtil.setBlackList(testToken,"logout", Duration.ofMillis(10));
        redisUtil.isBlackList(testToken);
        memberRepository.findByEmail(testEmail);

        // DB 블랙리스트 조회 성능 측정
        long dbTotalTimeTime = 0;
        for(int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            memberRepository.findByEmail(testEmail);
            dbTotalTimeTime += (System.nanoTime() - start);
        }
        long dbAverage = dbTotalTimeTime / iterations;

        // Redis 블랙리스트 조회 성능 측정
        long redisTotalTime = 0;
        for(int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            redisUtil.setBlackList(testToken, "logout", Duration.ofMillis(10));
        }
        long redisAverage = redisTotalTime / iterations;

        // 결과
        System.out.println("===" + iterations + "회 반복 조회 결과===");
        System.out.println("DB 평균 조회: " + dbAverage + "ns");
        System.out.println("Redis 평균 조회: " + redisAverage + "ns");

        assertThat(redisAverage).isLessThan(dbAverage);
    }

}
