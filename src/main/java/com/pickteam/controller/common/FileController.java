package com.pickteam.controller.common;

import com.pickteam.domain.common.FileInfo;
import com.pickteam.repository.common.FileInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileInfoRepository fileInfoRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    // 파일 다운로드
    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        try {
            FileInfo fileInfo = fileInfoRepository.findById(fileId)
                    .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));

            Path filePath = Paths.get(uploadDir).resolve(fileInfo.getNameHashed()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                throw new RuntimeException("파일이 존재하지 않습니다.");
            }

            String encodedFileName = URLEncoder.encode(fileInfo.getNameOrigin(), StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + encodedFileName + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("파일 다운로드 실패 - fileId: {}", fileId, e);
            throw new RuntimeException("파일 다운로드에 실패했습니다.", e);
        }
    }
}