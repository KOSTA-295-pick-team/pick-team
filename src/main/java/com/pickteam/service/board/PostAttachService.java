package com.pickteam.service.board;

import com.pickteam.domain.board.Post;
import com.pickteam.domain.board.PostAttach;
import com.pickteam.domain.common.FileInfo;
import com.pickteam.dto.board.PostAttachResponseDto;
import com.pickteam.repository.board.PostAttachRepository;
import com.pickteam.repository.board.PostRepository;
import com.pickteam.repository.common.FileInfoRepository;
import com.pickteam.service.security.SecurityAuditLogger;
import com.pickteam.util.FileSignatureValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PostAttachService {

    private final PostAttachRepository postAttachRepository;
    private final PostRepository postRepository;
    private final FileInfoRepository fileInfoRepository;
    private final SecurityAuditLogger securityAuditLogger;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.max-file-size}")
    private long maxFileSize;

    @Value("${app.upload.allowed-extensions}")
    private String allowedExtensions;

    @Value("${app.upload.allowed-mime-types}")
    private String allowedMimeTypes;

    @Value("${app.profile-image.max-size}")
    private long profileImageMaxSize;

    @Value("${app.profile-image.allowed-extensions}")
    private String profileImageAllowedExtensions;

    @Value("${app.profile-image.allowed-mime-types}")
    private String profileImageAllowedMimeTypes;

    @Value("${app.profile-image.dir}")
    private String profileImageDir;

    // 위험한 파일명 패턴 정의
    private static final Pattern DANGEROUS_PATTERN = Pattern.compile(
            "(?i).*\\.(exe|bat|cmd|com|pif|scr|vbs|js|jar|app|deb|pkg|dmg|sh|bin|run)$");

    // 파일명에서 허용하지 않는 문자들
    private static final Pattern INVALID_FILENAME_PATTERN = Pattern.compile("[<>:\"/\\\\|?*\\x00-\\x1f]");

    /**
     * 게시글 첨부파일 업로드 (보안 강화)
     */
    @Transactional
    public List<PostAttachResponseDto> uploadPostAttachments(Long postId, List<MultipartFile> files) {
        HttpServletRequest request = getCurrentRequest();

        // 게시글 존재 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + postId));

        return files.stream()
                .map(file -> uploadSinglePostAttachment(post, file, request))
                .collect(Collectors.toList());
    }

    /**
     * 단일 첨부파일 업로드 처리
     */
    private PostAttachResponseDto uploadSinglePostAttachment(Post post, MultipartFile file,
            HttpServletRequest request) {
        try {
            // 보안 검증 수행
            validateFileForUpload(file);

            // 파일 정보 생성 및 저장
            FileInfo fileInfo = createFileInfo(file, uploadDir);
            fileInfoRepository.save(fileInfo);

            // 첨부파일 정보 생성 및 저장
            PostAttach postAttach = PostAttach.builder()
                    .post(post)
                    .fileInfo(fileInfo)
                    .build();
            postAttachRepository.save(postAttach);

            // 보안 로깅
            securityAuditLogger.logFileUploadSuccess(
                    getCurrentUserId(),
                    getClientIpAddress(request),
                    fileInfo.getOriginalFileName(),
                    fileInfo.getFileSize());

            log.info("첨부파일 업로드 성공 - 게시글ID: {}, 파일: {}", post.getId(), fileInfo.getOriginalFileName());

            return PostAttachResponseDto.builder()
                    .id(postAttach.getId())
                    .originalFileName(fileInfo.getOriginalFileName())
                    .fileSize(fileInfo.getFileSize())
                    .contentType(fileInfo.getContentType())
                    .uploadDate(fileInfo.getCreatedAt())
                    .build();

        } catch (Exception e) {
            // 보안 로깅
            securityAuditLogger.logFileUploadFailure(
                    getCurrentUserId(),
                    getClientIpAddress(request),
                    file.getOriginalFilename(),
                    e.getMessage());

            log.error("첨부파일 업로드 실패 - 게시글ID: {}, 파일: {}", post.getId(), file.getOriginalFilename(), e);
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 파일 업로드 보안 검증
     */
    private void validateFileForUpload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }

        // 파일 크기 검증
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("파일 크기가 허용 범위를 초과했습니다");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("파일명이 유효하지 않습니다");
        }

        // 파일명 보안 검증
        validateFileName(originalFilename);

        // 확장자 검증
        validateFileExtension(originalFilename, allowedExtensions);

        // MIME 타입 검증
        validateMimeType(file.getContentType(), allowedMimeTypes);

        // 파일 시그니처 검증
        String extension = getFileExtension(originalFilename);
        if (!FileSignatureValidator.validateFileSignature(file, extension)) {
            throw new IllegalArgumentException("파일 시그니처가 유효하지 않습니다");
        }
    }

    /**
     * 프로필 이미지 업로드 (보안 강화)
     */
    @Transactional
    public FileInfo uploadProfileImage(Long userId, MultipartFile file) {
        HttpServletRequest request = getCurrentRequest();
        String clientIp = getClientIpAddress(request);

        try {
            log.info("프로필 이미지 업로드 시작 - userId: {}", userId);

            // 프로필 이미지 보안 검증
            validateProfileImageFile(file, userId, clientIp);

            // 기존 프로필 이미지 조회
            Optional<FileInfo> existingProfileImage = fileInfoRepository.findProfileImageByUserId(userId);

            // 새 파일 정보 생성 및 저장
            FileInfo newFileInfo = createFileInfo(file, profileImageDir);
            fileInfoRepository.save(newFileInfo);

            // 트랜잭션 완료 후 기존 파일 삭제 처리
            if (existingProfileImage.isPresent()) {
                scheduleOldProfileImageDeletion(existingProfileImage.get());
            }

            // 성공 로깅
            securityAuditLogger.logProfileImageUploadSuccess(
                    userId, clientIp, newFileInfo.getOriginalFileName(), newFileInfo.getFileSize());

            log.info("프로필 이미지 업로드 및 교체 완료 - userId: {}, newFileId: {}", userId, newFileInfo.getId());
            return newFileInfo;

        } catch (Exception e) {
            securityAuditLogger.logProfileImageUploadFailure(
                    userId, clientIp, file.getOriginalFilename(), e.getMessage());
            log.error("프로필 이미지 업로드 실패 - userId: {}", userId, e);
            throw new RuntimeException("프로필 이미지 업로드 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 프로필 이미지 파일 보안 검증
     */
    private void validateProfileImageFile(MultipartFile file, Long userId, String clientIp) {
        if (file.isEmpty()) {
            securityAuditLogger.logProfileImageMaliciousAttempt(userId, clientIp, "빈 파일 업로드 시도");
            throw new IllegalArgumentException("파일이 비어있습니다");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            securityAuditLogger.logProfileImageMaliciousAttempt(userId, clientIp, "유효하지 않은 파일명");
            throw new IllegalArgumentException("파일명이 유효하지 않습니다");
        }

        // 파일 크기 검증
        if (file.getSize() > profileImageMaxSize) {
            securityAuditLogger.logProfileImageSizeExceeded(userId, clientIp, file.getSize(), profileImageMaxSize);
            throw new IllegalArgumentException("프로필 이미지 파일 크기가 허용 범위를 초과했습니다");
        }

        // 확장자 검증
        if (!isValidProfileImageExtension(originalFilename)) {
            securityAuditLogger.logProfileImageExtensionMismatch(userId, clientIp, originalFilename);
            throw new IllegalArgumentException("허용되지 않는 파일 확장자입니다");
        }

        // MIME 타입 검증
        if (!isValidProfileImageMimeType(file.getContentType())) {
            securityAuditLogger.logProfileImageMimeTypeMismatch(userId, clientIp, file.getContentType());
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다");
        }

        // 파일 시그니처 검증
        try {
            if (!FileSignatureValidator.isValidFileSignature(file.getInputStream(), originalFilename)) {
                securityAuditLogger.logProfileImageSignatureMismatch(userId, clientIp, originalFilename);
                throw new IllegalArgumentException("파일 시그니처가 유효하지 않습니다");
            }
        } catch (IOException e) {
            securityAuditLogger.logProfileImageMaliciousAttempt(userId, clientIp, "파일 시그니처 검증 실패: " + e.getMessage());
            throw new RuntimeException("파일 시그니처 검증 중 오류가 발생했습니다", e);
        }

        // 파일명 보안 검증
        validateFileName(originalFilename);
    }

    /**
     * 프로필 이미지 확장자 검증
     */
    private boolean isValidProfileImageExtension(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        Set<String> allowedExtensionSet = Arrays.stream(profileImageAllowedExtensions.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        return allowedExtensionSet.contains(extension);
    }

    /**
     * 프로필 이미지 MIME 타입 검증
     */
    private boolean isValidProfileImageMimeType(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        Set<String> allowedMimeTypeSet = Arrays.stream(profileImageAllowedMimeTypes.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        return allowedMimeTypeSet.contains(mimeType.toLowerCase());
    }

    /**
     * 기존 프로필 이미지 삭제 스케줄링
     */
    private void scheduleOldProfileImageDeletion(FileInfo oldFileInfo) {
        String oldHashedFileName = oldFileInfo.getHashedFileName();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_COMMITTED) {
                    try {
                        deletePhysicalProfileImageFile(oldHashedFileName);
                        log.info("기존 프로필 이미지 파일 삭제 완료: {}", oldHashedFileName);
                    } catch (Exception e) {
                        log.error("기존 프로필 이미지 파일 삭제 실패: {}", oldHashedFileName, e);
                    }
                }
            }
        });
    }

    /**
     * 물리적 프로필 이미지 파일 삭제
     */
    private void deletePhysicalProfileImageFile(String hashedFileName) {
        try {
            Path filePath = Paths.get(profileImageDir, hashedFileName);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.debug("프로필 이미지 파일 삭제: {}", filePath);
            }
        } catch (IOException e) {
            log.error("프로필 이미지 파일 삭제 실패: {}", hashedFileName, e);
        }
    }

    /**
     * FileInfo 객체 생성
     */
    private FileInfo createFileInfo(MultipartFile file, String targetDir) throws IOException {
        String originalFilename = sanitizeFileName(file.getOriginalFilename());
        String hashedFileName = generateHashedFileName(originalFilename);

        // 디렉토리 생성
        Path uploadPath = Paths.get(targetDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 파일 저장
        Path filePath = uploadPath.resolve(hashedFileName);
        Files.copy(file.getInputStream(), filePath);

        return FileInfo.builder()
                .originalFileName(originalFilename)
                .hashedFileName(hashedFileName)
                .filePath(filePath.toString())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();
    }

    /**
     * 파일명 보안 검증
     */
    private void validateFileName(String filename) {
        // 위험한 확장자 검사
        if (DANGEROUS_PATTERN.matcher(filename).matches()) {
            throw new IllegalArgumentException("실행 가능한 파일은 업로드할 수 없습니다");
        }

        // 유효하지 않은 문자 검사
        if (INVALID_FILENAME_PATTERN.matcher(filename).find()) {
            throw new IllegalArgumentException("파일명에 허용되지 않는 문자가 포함되어 있습니다");
        }

        // 파일명 길이 검사
        if (filename.length() > 255) {
            throw new IllegalArgumentException("파일명이 너무 깁니다");
        }

        // 경로 탐색 공격 방지
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new IllegalArgumentException("파일명에 경로 문자가 포함되어 있습니다");
        }
    }

    /**
     * 파일 확장자 검증
     */
    private void validateFileExtension(String filename, String allowedExtensionsConfig) {
        String extension = getFileExtension(filename).toLowerCase();
        Set<String> allowedExtensionSet = Arrays.stream(allowedExtensionsConfig.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        if (!allowedExtensionSet.contains(extension)) {
            throw new IllegalArgumentException("허용되지 않는 파일 확장자입니다: " + extension);
        }
    }

    /**
     * MIME 타입 검증
     */
    private void validateMimeType(String mimeType, String allowedMimeTypesConfig) {
        if (mimeType == null) {
            throw new IllegalArgumentException("MIME 타입을 확인할 수 없습니다");
        }

        Set<String> allowedMimeTypeSet = Arrays.stream(allowedMimeTypesConfig.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        if (!allowedMimeTypeSet.contains(mimeType.toLowerCase())) {
            throw new IllegalArgumentException("허용되지 않는 MIME 타입입니다: " + mimeType);
        }
    }

    /**
     * 파일명 정리 (보안)
     */
    private String sanitizeFileName(String filename) {
        if (filename == null) {
            return "unknown";
        }

        // 기본 정리
        String sanitized = filename.trim();

        // 위험한 문자 제거
        sanitized = INVALID_FILENAME_PATTERN.matcher(sanitized).replaceAll("_");

        // 길이 제한
        if (sanitized.length() > 100) {
            String extension = getFileExtension(sanitized);
            String nameWithoutExt = sanitized.substring(0, sanitized.lastIndexOf('.'));
            sanitized = nameWithoutExt.substring(0, Math.min(nameWithoutExt.length(), 95 - extension.length())) + "."
                    + extension;
        }

        return sanitized;
    }

    /**
     * 해시된 파일명 생성
     */
    private String generateHashedFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid + (extension.isEmpty() ? "" : "." + extension);
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    /**
     * 현재 사용자 ID 조회
     */
    private Long getCurrentUserId() {
        // TODO: SecurityContext에서 현재 사용자 ID 가져오기
        return 1L; // 임시값
    }

    /**
     * 현재 HTTP 요청 조회
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attr.getRequest();
    }

    /**
     * 클라이언트 IP 주소 조회
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * 첨부파일 다운로드 (보안 강화)
     */
    @Transactional(readOnly = true)
    public PostAttachResponseDto downloadPostAttachment(Long attachmentId) {
        HttpServletRequest request = getCurrentRequest();

        try {
            PostAttach postAttach = postAttachRepository.findById(attachmentId)
                    .orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다: " + attachmentId));

            FileInfo fileInfo = postAttach.getFileInfo();

            // 파일 존재 확인
            Path filePath = Paths.get(fileInfo.getFilePath());
            if (!Files.exists(filePath)) {
                throw new IllegalArgumentException("파일이 존재하지 않습니다: " + fileInfo.getOriginalFileName());
            }

            // 경로 탐색 공격 방지
            Path normalizedPath = filePath.normalize();
            Path uploadPath = Paths.get(uploadDir).normalize();
            if (!normalizedPath.startsWith(uploadPath)) {
                securityAuditLogger.logFileDownloadMaliciousAttempt(
                        getCurrentUserId(),
                        getClientIpAddress(request),
                        "경로 탐색 공격 시도: " + fileInfo.getOriginalFileName());
                throw new SecurityException("허용되지 않는 파일 경로입니다");
            }

            // 성공 로깅
            securityAuditLogger.logFileDownloadSuccess(
                    getCurrentUserId(),
                    getClientIpAddress(request),
                    fileInfo.getOriginalFileName(),
                    fileInfo.getFileSize());

            return PostAttachResponseDto.builder()
                    .id(postAttach.getId())
                    .originalFileName(fileInfo.getOriginalFileName())
                    .fileSize(fileInfo.getFileSize())
                    .contentType(fileInfo.getContentType())
                    .uploadDate(fileInfo.getCreatedAt())
                    .build();

        } catch (Exception e) {
            securityAuditLogger.logFileDownloadFailure(
                    getCurrentUserId(),
                    getClientIpAddress(request),
                    "첨부파일 ID: " + attachmentId,
                    e.getMessage());

            log.error("첨부파일 다운로드 실패 - attachmentId: {}", attachmentId, e);
            throw new RuntimeException("파일 다운로드 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 프로필 이미지 삭제
     */
    @Transactional
    public void deleteProfileImage(Long userId) {
        HttpServletRequest request = getCurrentRequest();
        String clientIp = getClientIpAddress(request);

        try {
            Optional<FileInfo> profileImage = fileInfoRepository.findProfileImageByUserId(userId);

            if (profileImage.isPresent()) {
                FileInfo fileInfo = profileImage.get();
                String hashedFileName = fileInfo.getHashedFileName();

                // DB에서 삭제
                fileInfoRepository.delete(fileInfo);

                // 물리적 파일 삭제
                deletePhysicalProfileImageFile(hashedFileName);

                // 성공 로깅
                securityAuditLogger.logProfileImageDeleteSuccess(userId, clientIp, fileInfo.getOriginalFileName());

                log.info("프로필 이미지 삭제 완료 - userId: {}, fileName: {}", userId, fileInfo.getOriginalFileName());
            } else {
                log.info("삭제할 프로필 이미지가 없습니다 - userId: {}", userId);
            }

        } catch (Exception e) {
            securityAuditLogger.logProfileImageDeleteFailure(userId, clientIp, e.getMessage());
            log.error("프로필 이미지 삭제 실패 - userId: {}", userId, e);
            throw new RuntimeException("프로필 이미지 삭제 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 게시글별 첨부파일 목록 조회
     */
    @Transactional(readOnly = true)
    public List<PostAttachResponseDto> getPostAttachments(Long postId) {
        List<PostAttach> attachments = postAttachRepository.findByPostId(postId);

        return attachments.stream()
                .map(attach -> PostAttachResponseDto.builder()
                        .id(attach.getId())
                        .originalFileName(attach.getFileInfo().getOriginalFileName())
                        .fileSize(attach.getFileInfo().getFileSize())
                        .contentType(attach.getFileInfo().getContentType())
                        .uploadDate(attach.getFileInfo().getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 첨부파일 삭제
     */
    @Transactional
    public void deletePostAttachment(Long attachmentId) {
        HttpServletRequest request = getCurrentRequest();

        try {
            PostAttach postAttach = postAttachRepository.findById(attachmentId)
                    .orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다: " + attachmentId));

            FileInfo fileInfo = postAttach.getFileInfo();

            // DB에서 삭제
            postAttachRepository.delete(postAttach);
            fileInfoRepository.delete(fileInfo);

            // 물리적 파일 삭제
            try {
                Path filePath = Paths.get(fileInfo.getFilePath());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
            } catch (IOException e) {
                log.error("첨부파일 물리적 삭제 실패: {}", fileInfo.getFilePath(), e);
            }

            // 성공 로깅
            securityAuditLogger.logFileDeleteSuccess(
                    getCurrentUserId(),
                    getClientIpAddress(request),
                    fileInfo.getOriginalFileName());

            log.info("첨부파일 삭제 완료 - attachmentId: {}, fileName: {}", attachmentId, fileInfo.getOriginalFileName());

        } catch (Exception e) {
            securityAuditLogger.logFileDeleteFailure(
                    getCurrentUserId(),
                    getClientIpAddress(request),
                    "첨부파일 ID: " + attachmentId,
                    e.getMessage());

            log.error("첨부파일 삭제 실패 - attachmentId: {}", attachmentId, e);
            throw new RuntimeException("첨부파일 삭제 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}