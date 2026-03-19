package com.example.hungrypangproject.common.lock;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.common.exception.ServiceException;
import com.example.hungrypangproject.domain.coupon.service.CouponLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisLockAspect {

    private final CouponLockService couponLockService;

    @Around("@annotation(redisLock)")
    public Object withLock(ProceedingJoinPoint joinPoint, RedisLock redisLock) throws Throwable {
        Object[] args = joinPoint.getArgs();
        if (redisLock.argIndex() < 0 || redisLock.argIndex() >= args.length) {
            throw new ServiceException(ErrorCode.INVALID_INPUT_VALUE, "RedisLock argIndex가 메서드 파라미터 범위를 벗어났습니다.");
        }

        Object lockTarget = args[redisLock.argIndex()];
        if (lockTarget == null) {
            throw new ServiceException(ErrorCode.INVALID_INPUT_VALUE, "RedisLock 대상 파라미터는 null일 수 없습니다.");
        }

        String lockKey = redisLock.keyPrefix() + lockTarget;
        String lockToken = UUID.randomUUID().toString();
        Duration ttl = Duration.ofSeconds(redisLock.ttlSeconds());

        boolean locked = couponLockService.tryLock(lockKey, lockToken, ttl);
        if (!locked) {
            throw new ServiceException(ErrorCode.LOCK_IN_PROGRESS);
        }

        try {
            return joinPoint.proceed();
        } finally {
            couponLockService.unlock(lockKey, lockToken);
            log.debug("Redis lock released: {}", lockKey);
        }
    }
}

