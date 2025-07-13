package com.pickteam.repository.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.UUID;
import java.util.UUID;

/**
 * Redis 기반 SSE 세션 레포지토리
 */
@Repository
@RequiredArgsConstructor
public class SseSessionRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "sse:prepare:";
    private static final String TOKEN_PREFIX = "sse:token:";
    private static final String TOKEN_PREFIX = "sse:token:";
    private static final String TOKEN_PREFIX = "sse:token:";

    public void savePrepared(Long accountId) {

    public String savePreparedWithToken(Long accountId) {
        String token = UUID.randomUUID().toString();
        String tokenKey = TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(tokenKey, accountId.toString(), Duration.ofMinutes(2));
        return token;
    }


    public String savePreparedWithToken(Long accountId) {
        String token = UUID.randomUUID().toString();
        String tokenKey = TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(tokenKey, accountId.toString(), Duration.ofMinutes(2));
        return token;
    }
    public String savePreparedWithToken(Long accountId) {

    public String savePreparedWithToken(Long accountId) {
        String token = UUID.randomUUID().toString();
        String tokenKey = TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(tokenKey, accountId.toString(), Duration.ofMinutes(2));
        return token;
    }
        String token = UUID.randomUUID().toString();

    public String savePreparedWithToken(Long accountId) {
        String token = UUID.randomUUID().toString();
        String tokenKey = TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(tokenKey, accountId.toString(), Duration.ofMinutes(2));
        return token;
    }
        String tokenKey = TOKEN_PREFIX + token;

    public String savePreparedWithToken(Long accountId) {
        String token = UUID.randomUUID().toString();
        String tokenKey = TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(tokenKey, accountId.toString(), Duration.ofMinutes(2));
        return token;
    }
        redisTemplate.opsForValue().set(tokenKey, accountId.toString(), Duration.ofMinutes(2));

    public String savePreparedWithToken(Long accountId) {
        String token = UUID.randomUUID().toString();
        String tokenKey = TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(tokenKey, accountId.toString(), Duration.ofMinutes(2));
        return token;
    }
        return token;

    public String savePreparedWithToken(Long accountId) {
        String token = UUID.randomUUID().toString();
        String tokenKey = TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(tokenKey, accountId.toString(), Duration.ofMinutes(2));
        return token;
    }
    }

    public String savePreparedWithToken(Long accountId) {
        String token = UUID.randomUUID().toString();
        String tokenKey = TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(tokenKey, accountId.toString(), Duration.ofMinutes(2));
        return token;
    }


    public String savePreparedWithToken(Long accountId) {
        String token = UUID.randomUUID().toString();
        String tokenKey = TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(tokenKey, accountId.toString(), Duration.ofMinutes(2));
        return token;
    }
    public String savePreparedWithToken(Long accountId) {

    public String savePreparedWithToken(Long accountId) {
        String token = UUID.randomUUID().toString();
        String tokenKey = TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(tokenKey, accountId.toString(), Duration.ofMinutes(2));
        return token;
    }
        String token = UUID.randomUUID().toString();

    public String savePreparedWithToken(Long accountId) {
        String token = UUID.randomUUID().toString();
        String tokenKey = TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(tokenKey, accountId.toString(), Duration.ofMinutes(2));
        return token;
    }
        String tokenKey = TOKEN_PREFIX + token;

    public String savePreparedWithToken(Long accountId) {
        String token = UUID.randomUUID().toString();
        String tokenKey = TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(tokenKey, accountId.toString(), Duration.ofMinutes(2));
        return token;
    }
        redisTemplate.opsForValue().set(tokenKey, accountId.toString(), Duration.ofMinutes(2));

    public String savePreparedWithToken(Long accountId) {
        String token = UUID.randomUUID().toString();
        String tokenKey = TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(tokenKey, accountId.toString(), Duration.ofMinutes(2));
        return token;
    }
        return token;

    public String savePreparedWithToken(Long accountId) {
        String token = UUID.randomUUID().toString();
        String tokenKey = TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(tokenKey, accountId.toString(), Duration.ofMinutes(2));
        return token;
    }
    }

    public String savePreparedWithToken(Long accountId) {
        String token = UUID.randomUUID().toString();
        String tokenKey = TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(tokenKey, accountId.toString(), Duration.ofMinutes(2));
        return token;
    }
        String key = PREFIX + accountId;

    public String savePreparedWithToken(Long accountId) {
        String token = UUID.randomUUID().toString();
        String tokenKey = TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(tokenKey, accountId.toString(), Duration.ofMinutes(2));
        return token;
    }
        redisTemplate.opsForValue().set(key, "true", Duration.ofMinutes(2)); // TTL 설정

    public String savePreparedWithToken(Long accountId) {
        String token = UUID.randomUUID().toString();
        String tokenKey = TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(tokenKey, accountId.toString(), Duration.ofMinutes(2));
        return token;
    }
    }

    public String savePreparedWithToken(Long accountId) {
        String token = UUID.randomUUID().toString();
        String tokenKey = TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(tokenKey, accountId.toString(), Duration.ofMinutes(2));
        return token;
    }

    public boolean isPrepared(Long accountId) {

    public Optional<Long> resolvePreparedByToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        String accountId = redisTemplate.opsForValue().get(tokenKey);
        if (accountId != null) {
            redisTemplate.delete(tokenKey); // 토큰 사용 후 즉시 삭제
            return Optional.of(Long.parseLong(accountId));
        }
        return Optional.empty();
    }


    public Optional<Long> resolvePreparedByToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        String accountId = redisTemplate.opsForValue().get(tokenKey);
        if (accountId != null) {
            redisTemplate.delete(tokenKey); // 토큰 사용 후 즉시 삭제
            return Optional.of(Long.parseLong(accountId));
        }
        return Optional.empty();
    }
    public Optional<Long> resolvePreparedByToken(String token) {

    public Optional<Long> resolvePreparedByToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        String accountId = redisTemplate.opsForValue().get(tokenKey);
        if (accountId != null) {
            redisTemplate.delete(tokenKey); // 토큰 사용 후 즉시 삭제
            return Optional.of(Long.parseLong(accountId));
        }
        return Optional.empty();
    }
        String tokenKey = TOKEN_PREFIX + token;

    public Optional<Long> resolvePreparedByToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        String accountId = redisTemplate.opsForValue().get(tokenKey);
        if (accountId != null) {
            redisTemplate.delete(tokenKey); // 토큰 사용 후 즉시 삭제
            return Optional.of(Long.parseLong(accountId));
        }
        return Optional.empty();
    }
        String accountIdStr = redisTemplate.opsForValue().get(tokenKey);

    public Optional<Long> resolvePreparedByToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        String accountId = redisTemplate.opsForValue().get(tokenKey);
        if (accountId != null) {
            redisTemplate.delete(tokenKey); // 토큰 사용 후 즉시 삭제
            return Optional.of(Long.parseLong(accountId));
        }
        return Optional.empty();
    }
        if (accountIdStr != null) {

    public Optional<Long> resolvePreparedByToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        String accountId = redisTemplate.opsForValue().get(tokenKey);
        if (accountId != null) {
            redisTemplate.delete(tokenKey); // 토큰 사용 후 즉시 삭제
            return Optional.of(Long.parseLong(accountId));
        }
        return Optional.empty();
    }
            redisTemplate.delete(tokenKey); // 토큰 사용 후 즉시 삭제

    public Optional<Long> resolvePreparedByToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        String accountId = redisTemplate.opsForValue().get(tokenKey);
        if (accountId != null) {
            redisTemplate.delete(tokenKey); // 토큰 사용 후 즉시 삭제
            return Optional.of(Long.parseLong(accountId));
        }
        return Optional.empty();
    }
            return Optional.of(Long.parseLong(accountIdStr));

    public Optional<Long> resolvePreparedByToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        String accountId = redisTemplate.opsForValue().get(tokenKey);
        if (accountId != null) {
            redisTemplate.delete(tokenKey); // 토큰 사용 후 즉시 삭제
            return Optional.of(Long.parseLong(accountId));
        }
        return Optional.empty();
    }
        }

    public Optional<Long> resolvePreparedByToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        String accountId = redisTemplate.opsForValue().get(tokenKey);
        if (accountId != null) {
            redisTemplate.delete(tokenKey); // 토큰 사용 후 즉시 삭제
            return Optional.of(Long.parseLong(accountId));
        }
        return Optional.empty();
    }
        return Optional.empty();
    }
        String key = PREFIX + accountId;

    public Optional<Long> resolvePreparedByToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        String accountIdStr = redisTemplate.opsForValue().get(tokenKey);
        if (accountIdStr != null) {
            redisTemplate.delete(tokenKey); // 토큰 사용 후 즉시 삭제
            return Optional.of(Long.parseLong(accountIdStr));
        }
        return Optional.empty();
    }
        return "true".equals(redisTemplate.opsForValue().get(key));

    public Optional<Long> resolvePreparedByToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        String accountIdStr = redisTemplate.opsForValue().get(tokenKey);
        if (accountIdStr != null) {
            redisTemplate.delete(tokenKey); // 토큰 사용 후 즉시 삭제
            return Optional.of(Long.parseLong(accountIdStr));
        }
        return Optional.empty();
    }
    }

    public Optional<Long> resolvePreparedByToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        String accountIdStr = redisTemplate.opsForValue().get(tokenKey);
        if (accountIdStr != null) {
            redisTemplate.delete(tokenKey); // 토큰 사용 후 즉시 삭제
            return Optional.of(Long.parseLong(accountIdStr));
        }
        return Optional.empty();
    }

    public void deletePrepared(Long accountId) {

    public Optional<Long> resolvePreparedByToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        String accountId = redisTemplate.opsForValue().get(tokenKey);
        if (accountId != null) {
            redisTemplate.delete(tokenKey); // 토큰 사용 후 즉시 삭제
            return Optional.of(Long.parseLong(accountId));
        }
        return Optional.empty();
    }
        String key = PREFIX + accountId;

    public Optional<Long> resolvePreparedByToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        String accountId = redisTemplate.opsForValue().get(tokenKey);
        if (accountId != null) {
            redisTemplate.delete(tokenKey); // 토큰 사용 후 즉시 삭제
            return Optional.of(Long.parseLong(accountId));
        }
        return Optional.empty();
    }
        redisTemplate.delete(key);

    public Optional<Long> resolvePreparedByToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        String accountId = redisTemplate.opsForValue().get(tokenKey);
        if (accountId != null) {
            redisTemplate.delete(tokenKey); // 토큰 사용 후 즉시 삭제
            return Optional.of(Long.parseLong(accountId));
        }
        return Optional.empty();
    }
    }

    public Optional<Long> resolvePreparedByToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        String accountId = redisTemplate.opsForValue().get(tokenKey);
        if (accountId != null) {
            redisTemplate.delete(tokenKey); // 토큰 사용 후 즉시 삭제
            return Optional.of(Long.parseLong(accountId));
        }
        return Optional.empty();
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