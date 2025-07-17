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
        if (keys == null || keys.isEmpty()) {
            return Optional.empty();
        }
        
        // 여러 키가 있을 경우 하나씩 시도하며 동시성 문제 해결
        for (String key : keys) {
            try {
                Long accountId = Long.parseLong(key.replace(PREFIX, ""));
                
                // 키가 존재하는지 확인하고 원자적으로 삭제
                Boolean deleted = redisTemplate.delete(key);
                if (Boolean.TRUE.equals(deleted)) {
                    return Optional.of(accountId);
                }
            } catch (NumberFormatException e) {
                // 잘못된 키 형식은 무시
                continue;
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * 준비된 세션 수 조회 (디버깅용)
     */
    public long countPreparedSessions() {
        Set<String> keys = redisTemplate.keys(PREFIX + "*");
        return keys != null ? keys.size() : 0;
    }
}