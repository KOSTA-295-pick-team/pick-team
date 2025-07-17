package com.pickteam.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * HTTP 클라이언트 및 Jackson ObjectMapper 설정
 * - OAuth API 호출에 적합한 RestTemplate 타임아웃 설정
 * - Java 8 날짜/시간 타입 지원
 * - LocalDateTime 직렬화/역직렬화
 * - 전체 애플리케이션에서 사용하는 ObjectMapper 설정
 */
@Configuration
public class HttpClientJacksonConfig {

    /**
     * OAuth API 호출용 RestTemplate Bean
     * - 연결 타임아웃: 5초
     * - 읽기 타임아웃: 10초
     * 
     * @return 설정된 RestTemplate 인스턴스
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(clientHttpRequestFactory());
        return restTemplate;
    }

    /**
     * HTTP 클라이언트 요청 팩토리 설정
     * 
     * @return 타임아웃이 설정된 ClientHttpRequestFactory
     */
    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // OAuth API 호출에 적합한 타임아웃 설정
        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(10).toMillis());

        return factory;
    }

    /**
     * Jackson ObjectMapper Bean 설정 (Primary)
     * - JSR310 (Java 8 Time) 모듈 등록
     * - LocalDateTime 직렬화 지원
     * - OAuth API 응답 파싱 및 전체 애플리케이션에서 사용
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Java 8 시간 모듈 등록
        mapper.registerModule(new JavaTimeModule());

        // 타임스탬프 대신 ISO 문자열 사용
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 알 수 없는 필드 무시
        mapper.disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // 빈 객체 직렬화 허용
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS);

        return mapper;
    }
}
