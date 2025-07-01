package com.pickteam.service.user;

import com.pickteam.exception.validation.ValidationException;
import com.pickteam.constants.FileUploadErrorMessages;
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
        log.info("프로필 이미지 업로드 시작: userId={}, fileSize={}", userId, file.getSize());

        // 1. 파일 유효성 검증
        if (!isValidImageFile(file)) {
            throw new ValidationException(FileUploadErrorMessages.UNSUPPORTED_FILE_FORMAT);
        }

        // 2. 파일 크기 검증 (프로필 이미지 전용 크기 사용)
        if (file.getSize() > profileImageMaxSize) {
            long maxSizeMB = profileImageMaxSize / 1024 / 1024;
            throw new ValidationException(String.format(FileUploadErrorMessages.FILE_SIZE_EXCEEDED, maxSizeMB));
        }

        try {
            // 3. 업로드 디렉토리 생성 및 보안 검증
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 4. 원본 파일명 보안 검증
            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null || originalFileName.trim().isEmpty()) {
                throw new ValidationException(FileUploadErrorMessages.INVALID_FILE_NAME);
            }

            // 보안: 원본 파일명 정제
            String sanitizedOriginalName = sanitizeFileName(originalFileName);
            String fileExtension = getFileExtension(sanitizedOriginalName);

            // 5. 고유하고 안전한 파일명 생성
            String fileName = "profile_" + userId + "_" + UUID.randomUUID().toString() + fileExtension;

            // 보안: 생성된 파일명도 검증
            if (!isSecureFileName(fileName)) {
                throw new ValidationException(FileUploadErrorMessages.INSECURE_FILE_NAME);
            }

            // 6. 파일 저장 (경로 보안 검증)
            Path filePath = uploadPath.resolve(fileName);

            // 보안: 파일 경로가 업로드 디렉토리 내부인지 확인
            if (!filePath.normalize().startsWith(uploadPath.normalize())) {
                throw new ValidationException(FileUploadErrorMessages.INVALID_FILE_PATH);
            }

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 7. 접근 URL 생성
            String fileUrl = baseUrl + "/uploads/" + fileName;
            log.info("프로필 이미지 업로드 완료: userId={}, fileSize={}", userId, file.getSize());

            return fileUrl;

        } catch (ValidationException e) {
            // ValidationException은 그대로 재발생 (사용자에게 보여줄 메시지)
            throw e;
        } catch (IOException e) {
            // 보안: 상세한 에러 정보는 로그에만 기록하고, 사용자에게는 일반적인 메시지만 전달
            log.error("프로필 이미지 업로드 실패: userId={}, errorType={}", userId, e.getClass().getSimpleName());
            log.debug("상세 에러 정보: ", e); // DEBUG 레벨로만 스택트레이스 출력
            throw new ValidationException(FileUploadErrorMessages.FILE_UPLOAD_FAILED);
        } catch (Exception e) {
            // 예상치 못한 에러에 대한 보안 처리
            log.error("예상치 못한 파일 업로드 에러: userId={}, errorType={}", userId, e.getClass().getSimpleName());
            log.debug("상세 에러 정보: ", e);
            throw new ValidationException(FileUploadErrorMessages.FILE_PROCESSING_ERROR);
        }
    }

    @Override
    public void deleteProfileImage(String imageUrl, Long userId) {
        log.info("프로필 이미지 삭제 시작: userId={}", userId);

        try {
            // URL에서 파일명 추출
            String fileName = extractFileNameFromUrl(imageUrl);
            if (fileName == null) {
                log.warn("유효하지 않은 이미지 URL 형식");
                return;
            }

            // 파일 경로 생성
            Path filePath = Paths.get(uploadDir).resolve(fileName);

            // 파일 존재 여부 확인 후 삭제
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("프로필 이미지 삭제 완료: userId={}", userId);
            } else {
                log.warn("삭제할 파일이 존재하지 않음: userId={}", userId);
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

        String fileName = url.substring(lastSlashIndex + 1);

        // 보안: 파일명 검증 (경로 조작 공격 방지)
        if (!isSecureFileName(fileName)) {
            log.warn("보안 위험이 있는 파일명 감지: [FILENAME_MASKED]");
            return null;
        }

        return fileName;
    }

    /**
     * 안전한 파일명인지 검증
     * - 경로 조작 공격 방지 (../, ..\\ 등)
     * - 특수문자 제한
     * - 파일명 길이 제한
     */
    private boolean isSecureFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }

        // 1. 경로 조작 패턴 차단
        if (fileName.contains("../") || fileName.contains("..\\") ||
                fileName.contains("/") || fileName.contains("\\")) {
            return false;
        }

        // 2. 위험한 문자들 차단
        if (fileName.contains("<") || fileName.contains(">") ||
                fileName.contains("|") || fileName.contains("?") ||
                fileName.contains("*") || fileName.contains(":")) {
            return false;
        }

        // 3. 파일명 길이 제한 (255자)
        if (fileName.length() > 255) {
            return false;
        }

        // 4. 숨김 파일 차단
        if (fileName.startsWith(".")) {
            return false;
        }

        return true;
    }

    /**
     * 안전한 파일명 생성
     * - 원본 파일명을 기반으로 안전한 파일명 생성
     * - 위험한 문자들을 안전한 문자로 대체
     */
    private String sanitizeFileName(String originalFileName) {
        if (originalFileName == null) {
            return "unknown";
        }

        // 경로 구분자와 위험한 문자들을 언더스코어로 대체
        String sanitized = originalFileName
                .replaceAll("[/\\\\<>|?*:\"]", "_")
                .replaceAll("[\\s]+", "_"); // 공백을 언더스코어로

        // 파일명이 너무 길면 잘라내기
        if (sanitized.length() > 100) {
            String extension = getFileExtension(sanitized);
            String nameWithoutExt = sanitized.substring(0, sanitized.lastIndexOf('.'));
            sanitized = nameWithoutExt.substring(0, Math.min(nameWithoutExt.length(), 100 - extension.length()))
                    + extension;
        }

        return sanitized;
    }
}
