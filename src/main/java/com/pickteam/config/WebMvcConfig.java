package com.pickteam.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
/**
 * *** 파일 업로드 기능에 대한 중복 구현으로
 * 선행 구현체를 사용하기로 합의함.
 * 구현 방식이 달라 사용된 연관 클래스들을 남겨둠 ***
 *
 * 웹 MVC 설정
 * - 정적 리소스 핸들링 설정
 * - 업로드된 파일 서빙 설정
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Value("${app.profile.image-dir}") // 필요 시 환경변수 교체
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 업로드된 파일 서빙
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/")
                .setCachePeriod(3600); // 1시간 캐시
    }
}
