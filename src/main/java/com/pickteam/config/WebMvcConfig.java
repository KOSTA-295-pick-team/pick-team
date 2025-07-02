package com.pickteam.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 웹 MVC 설정
 * - 정적 리소스 핸들링 설정
 * - 업로드된 파일 서빙 설정
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.profile.image-dir}")
    private String profileImageDir;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 프로필 이미지 서빙
        registry.addResourceHandler("/profile-images/**")
                .addResourceLocations("file:" + profileImageDir + "/")
                .setCachePeriod(3600); // 1시간 캐시

        // 일반 업로드 파일 서빙
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/")
                .setCachePeriod(3600); // 1시간 캐시
    }
}
