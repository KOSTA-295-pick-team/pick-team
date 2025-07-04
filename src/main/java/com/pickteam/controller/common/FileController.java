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
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileInfoRepository fileInfoRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    // 위험한 파일명 패턴 정의 (경로 탐색 공격 방지)
    private static final Pattern DANGEROUS_PATH_PATTERN = Pattern.compile(".*[/\\\\].*|.*\\.\\..*");

    // 파일 다운로드 (보안 강화)
    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        try {
            // 1. FileInfo 조회
            FileInfo fileInfo = fileInfoRepository.findById(fileId)
                    .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));

            // 2. 보안 검증 수행
            validateFileAccess(fileInfo);

            // 3. 파일 다운로드 제공
            Path filePath = createSecureFilePath(fileInfo.getNameHashed());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                log.warn("요청된 파일이 존재하지 않음 - fileId: {}, path: {}", fileId, filePath);
                throw new RuntimeException("파일이 존재하지 않습니다.");
            }

            String encodedFileName = URLEncoder.encode(fileInfo.getNameOrigin(), StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            log.info("파일 다운로드 성공 - fileId: {}, fileName: {}",
                    fileId, fileInfo.getNameOrigin());

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

    /**
     * 파일 접근 보안 검증
     * - 경로 탐색 공격 방지
     * - 위험한 파일명 패턴 차단
     */
    private void validateFileAccess(FileInfo fileInfo) {
        String hashedFileName = fileInfo.getNameHashed();

        // 파일명이 null이거나 비어있는지 확인
        if (hashedFileName == null || hashedFileName.trim().isEmpty()) {
            throw new SecurityException("유효하지 않은 파일명입니다.");
        }

        // 경로 탐색 공격 패턴 검사
        if (DANGEROUS_PATH_PATTERN.matcher(hashedFileName).matches()) {
            log.warn("경로 탐색 공격 시도 탐지 - fileName: {}", hashedFileName);
            throw new SecurityException("허용되지 않는 파일 경로입니다.");
        }

        // 파일명에 위험한 문자가 포함되어 있는지 확인
        if (hashedFileName.contains("../") || hashedFileName.contains("..\\") ||
                hashedFileName.contains("/") || hashedFileName.contains("\\")) {
            log.warn("위험한 경로 문자 탐지 - fileName: {}", hashedFileName);
            throw new SecurityException("허용되지 않는 파일명입니다.");
        }

        log.debug("파일 보안 검증 통과 - fileName: {}", hashedFileName);
    }

    /**
     * 보안이 강화된 파일 경로 생성
     * - 업로드 디렉토리 기준으로 정규화된 경로 생성
     * - 경로 이탈 방지
     */
    private Path createSecureFilePath(String hashedFileName) {
        // 업로드 디렉토리를 정규화
        Path uploadPath = Paths.get(uploadDir).normalize().toAbsolutePath();

        // 해시된 파일명으로 경로 생성 및 정규화
        Path filePath = uploadPath.resolve(hashedFileName).normalize();

        // 경로가 업로드 디렉토리 내부에 있는지 확인 (경로 탐색 공격 방지)
        if (!filePath.startsWith(uploadPath)) {
            log.error("경로 탐색 공격 시도 - uploadDir: {}, requestPath: {}", uploadPath, filePath);
            throw new SecurityException("허용되지 않는 파일 경로입니다.");
        }

        log.debug("보안 파일 경로 생성 완료 - path: {}", filePath);
        return filePath;
    }
}