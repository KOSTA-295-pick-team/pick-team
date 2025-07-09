package com.pickteam.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 설정 클래스
 * - RedisTemplate 설정
 * - JSON 직렬화/역직렬화 설정
 * - RestTemplateConfig에서 정의한 ObjectMapper 사용
 */
@Configuration
public class RedisConfig {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * RedisTemplate 설정
     * - Key: String 직렬화
     * - Value: JSON 직렬화 (주입받은 ObjectMapper 사용)
     * 
     * @param connectionFactory Redis 연결 팩토리
     * @return 설정된 RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // String 직렬화
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // JSON 직렬화 (주입받은 ObjectMapper 사용)
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // Key와 HashKey는 String 직렬화
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);

        // Value와 HashValue는 JSON 직렬화
        template.setValueSerializer(jsonRedisSerializer);
        template.setHashValueSerializer(jsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
