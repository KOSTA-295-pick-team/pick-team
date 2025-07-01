package com.pickteam.service.user;

import com.pickteam.exception.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

/**
 * 파일 업로드 서비스 구현체
 * - 로컬 파일 시스템에 파일 저장
 * - 추후 클라우드 스토리지(AWS S3 등)로 확장 가능
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    @Value("${app.file.upload-dir}")
    private String uploadDir;

    @Value("${app.file.max-size}")
    private long maxFileSize;

    @Value("${app.file.base-url}")
    private String baseUrl;

    // 프로필 이미지 전용 설정 (환경변수에서 주입)
    @Value("${app.profile-image.allowed-extensions}")
    private String allowedExtensionsConfig;

    @Value("${app.profile-image.allowed-mime-types}")
    private String allowedMimeTypesConfig;

    @Value("${app.profile-image.max-size}")
    private long profileImageMaxSize;

    @Override
    public String uploadProfileImage(MultipartFile file, Long userId) {
        log.info("프로필 이미지 업로드 시작: userId={}, fileName={}", userId, file.getOriginalFilename());

        // 1. 파일 유효성 검증
        if (!isValidImageFile(file)) {
            throw new ValidationException("지원하지 않는 파일 형식입니다. JPG, PNG, GIF, WEBP 파일만 업로드 가능합니다.");
        }

        // 2. 파일 크기 검증 (프로필 이미지 전용 크기 사용)
        if (file.getSize() > profileImageMaxSize) {
            throw new ValidationException(
                    "파일 크기가 너무 큽니다. 최대 " + (profileImageMaxSize / 1024 / 1024) + "MB까지 업로드 가능합니다.");
        }

        try {
            // 3. 업로드 디렉토리 생성
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 4. 고유한 파일명 생성
            String originalFileName = Objects.requireNonNull(file.getOriginalFilename());
            String fileExtension = getFileExtension(originalFileName);
            String fileName = "profile_" + userId + "_" + UUID.randomUUID().toString() + fileExtension;

            // 5. 파일 저장
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 6. 접근 URL 생성
            String fileUrl = baseUrl + "/uploads/" + fileName;
            log.info("프로필 이미지 업로드 완료: userId={}, fileUrl={}", userId, fileUrl);

            return fileUrl;

        } catch (IOException e) {
            log.error("프로필 이미지 업로드 실패: userId={}, error={}", userId, e.getMessage(), e);
            throw new ValidationException("파일 업로드 중 오류가 발생했습니다.");
        }
    }

    @Override
    public void deleteProfileImage(String imageUrl, Long userId) {
        log.info("프로필 이미지 삭제 시작: userId={}, imageUrl={}", userId, imageUrl);

        try {
            // URL에서 파일명 추출
            String fileName = extractFileNameFromUrl(imageUrl);
            if (fileName == null) {
                log.warn("유효하지 않은 이미지 URL: {}", imageUrl);
                return;
            }

            // 파일 경로 생성
            Path filePath = Paths.get(uploadDir).resolve(fileName);

            // 파일 존재 여부 확인 후 삭제
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("프로필 이미지 삭제 완료: userId={}, fileName={}", userId, fileName);
            } else {
                log.warn("삭제할 파일이 존재하지 않음: userId={}, fileName={}", userId, fileName);
            }

        } catch (IOException e) {
            log.error("프로필 이미지 삭제 실패: userId={}, error={}", userId, e.getMessage(), e);
            // 파일 삭제 실패는 치명적이지 않으므로 예외를 던지지 않음
        }
    }

    @Override
    public boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // 1. 파일명 확장자 검증
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            return false;
        }

        String fileExtension = getFileExtension(originalFileName).toLowerCase();
        if (fileExtension.startsWith(".")) {
            fileExtension = fileExtension.substring(1); // . 제거
        }

        // 환경변수에서 허용 확장자 배열 생성
        String[] allowedExtensions = allowedExtensionsConfig.split(",");
        boolean validExtension = false;
        for (String allowedExt : allowedExtensions) {
            if (fileExtension.equals(allowedExt.trim())) {
                validExtension = true;
                break;
            }
        }

        if (!validExtension) {
            return false;
        }

        // 2. MIME 타입 검증
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        // 환경변수에서 허용 MIME 타입 배열 생성
        String[] allowedMimeTypes = allowedMimeTypesConfig.split(",");
        boolean validMimeType = false;
        for (String allowedMime : allowedMimeTypes) {
            if (contentType.equals(allowedMime.trim())) {
                validMimeType = true;
                break;
            }
        }

        return validMimeType;
    }

    /**
     * 파일명에서 확장자 추출
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex);
    }

    /**
     * URL에서 파일명 추출
     */
    private String extractFileNameFromUrl(String url) {
        if (url == null || !url.contains("/uploads/")) {
            return null;
        }

        int lastSlashIndex = url.lastIndexOf('/');
        if (lastSlashIndex == -1 || lastSlashIndex == url.length() - 1) {
            return null;
        }

        return url.substring(lastSlashIndex + 1);
    }
}
