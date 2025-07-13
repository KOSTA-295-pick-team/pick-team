package com.pickteam.repository.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

/**
 * Redis 기반 SSE 세션 레포지토리
 */
@Repository
@RequiredArgsConstructor
public class SseSessionRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "sse:prepare:";

    public void savePrepared(Long accountId) {
        String key = PREFIX + accountId;
        redisTemplate.opsForValue().set(key, "true", Duration.ofMinutes(2)); // TTL 설정
    }

    public boolean isPrepared(Long accountId) {
        String key = PREFIX + accountId;
        return "true".equals(redisTemplate.opsForValue().get(key));
    }

    public void deletePrepared(Long accountId) {
        String key = PREFIX + accountId;
        redisTemplate.delete(key);
    }

    public Optional<Long> resolveAnyPrepared() {
        Set<String> keys = redisTemplate.keys(PREFIX + "*");
        if (keys == null || keys.isEmpty()) return Optional.empty();
        String anyKey = keys.iterator().next();
        Long accountId = Long.parseLong(anyKey.replace(PREFIX, ""));
        deletePrepared(accountId);
        return Optional.of(accountId);
    }
}