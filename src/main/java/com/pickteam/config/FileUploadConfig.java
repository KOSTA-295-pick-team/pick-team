package com.pickteam.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
@Slf4j
public class FileUploadConfig {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
            log.info("업로드 디렉토리 생성/확인 완료: {}", uploadDir);
        } catch (IOException e) {
            log.error("업로드 디렉토리 생성 실패: {}", uploadDir, e);
        }
    }
}