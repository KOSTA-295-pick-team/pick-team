package com.pickteam.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * RestTemplate 및 HTTP 클라이언트 설정
 * - OAuth API 호출에 적합한 타임아웃 및 설정 적용
 * - ObjectMapper Bean 정의
 */
@Configuration
public class RestTemplateConfig {

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
     * JSON 파싱용 ObjectMapper Bean
     * - OAuth API 응답 파싱에 사용
     * 
     * @return ObjectMapper 인스턴스
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
