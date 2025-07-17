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
        // 프로필 이미지만 직접 웹 서빙 허용 (공개 접근 필요)
        registry.addResourceHandler("/profile-images/**")
                .addResourceLocations("file:" + profileImageDir + "/")
                .setCachePeriod(3600); // 1시간 캐시

        // 워크스페이스 아이콘 파일 웹 서빙 허용 (공개 접근 필요)
        registry.addResourceHandler("/uploads/workspace-icons/**")
                .addResourceLocations("file:" + uploadDir + "/workspace-icons/")
                .setCachePeriod(3600); // 1시간 캐시

        // 일반 업로드 파일 직접 웹 접근 차단 (보안 강화)
        // 파일 다운로드는 /api/files/{fileId}/download 컨트롤러를 통해서만 허용
        // registry.addResourceHandler("/uploads/**")
        // .addResourceLocations("file:" + uploadDir + "/")
        // .setCachePeriod(3600);
    }
}
