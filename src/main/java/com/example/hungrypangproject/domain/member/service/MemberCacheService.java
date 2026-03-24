package com.example.hungrypangproject.domain.member.service;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.common.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class MemberCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String CACHE_REFRESH_TOKEN_PREFIX = "Refresh:";

    // RefreshToken 캐시 저장
    public void saveRefreshToken(String email, String refreshToken, Long durationMillis) {
        String key = CACHE_REFRESH_TOKEN_PREFIX + email;
        try {
            redisTemplate.opsForValue().set(key, refreshToken, Duration.ofMillis(durationMillis));
        } catch (RedisConnectionFailureException e) {
            throw new ServiceException(ErrorCode.REDIS_CONNECTION_FAILED);
        }

    }
        // RefreshToken 가져오기
    public String getRefreshToken(String email) {
        String key = CACHE_REFRESH_TOKEN_PREFIX + email;
        return (String) redisTemplate.opsForValue().get(key);
    }

    // RefreshToken 캐시 삭제 (logout)
    public void deleteRefreshToken (String email) {
        redisTemplate.delete(CACHE_REFRESH_TOKEN_PREFIX + email);
    }

}
