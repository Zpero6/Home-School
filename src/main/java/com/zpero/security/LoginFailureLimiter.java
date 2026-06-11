package com.zpero.security;

import com.zpero.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LoginFailureLimiter {

    private static final int MAX_FAILURE_COUNT = 5;
    private static final Duration WINDOW = Duration.ofMinutes(30);
    private static final String FAIL_PREFIX = "login:fail:";
    private static final String LOCK_PREFIX = "login:lock:";

    private final StringRedisTemplate redisTemplate;

    public void assertNotLocked(String channel, String account) {
        if (!StringUtils.hasText(account)) {
            return;
        }
        Boolean locked = redisTemplate.hasKey(lockKey(channel, account));
        if (Boolean.TRUE.equals(locked)) {
            throw new BusinessException(423, "密码错误次数过多，账号已锁定30分钟");
        }
    }

    public boolean recordFailure(String channel, String account) {
        if (!StringUtils.hasText(account)) {
            return false;
        }

        String failKey = failKey(channel, account);
        Long count = redisTemplate.opsForValue().increment(failKey);
        if (count != null && count == 1L) {
            redisTemplate.expire(failKey, WINDOW);
        }

        if (count != null && count >= MAX_FAILURE_COUNT) {
            redisTemplate.opsForValue().set(lockKey(channel, account), "1", WINDOW);
            redisTemplate.delete(failKey);
            return true;
        }
        return false;
    }

    public void clear(String channel, String account) {
        if (!StringUtils.hasText(account)) {
            return;
        }
        redisTemplate.delete(List.of(failKey(channel, account), lockKey(channel, account)));
    }

    private String failKey(String channel, String account) {
        return FAIL_PREFIX + channel + ":" + account;
    }

    private String lockKey(String channel, String account) {
        return LOCK_PREFIX + channel + ":" + account;
    }
}
