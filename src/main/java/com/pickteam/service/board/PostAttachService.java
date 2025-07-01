package com.pickteam.service.board;

import com.pickteam.domain.board.Post;
import com.pickteam.domain.board.PostAttach;
import com.pickteam.domain.common.FileInfo;
import com.pickteam.dto.board.PostAttachResponseDto;
import com.pickteam.repository.board.PostAttachRepository;
import com.pickteam.repository.board.PostRepository;
import com.pickteam.repository.common.FileInfoRepository;
import com.pickteam.domain.user.Account;
import com.pickteam.repository.user.AccountRepository;
import com.pickteam.exception.validation.ValidationException;
import com.pickteam.constants.FileUploadErrorMessages;
import java.nio.file.StandardCopyOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PostAttachService {

    private final PostAttachRepository postAttachRepository;
    private final PostRepository postRepository;
    private final FileInfoRepository fileInfoRepository;
    // === 프로필 이미지 기능을 위한 추가 의존성 ===
    private final AccountRepository accountRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.max-file-size}")
    private String maxFileSize;

    @Value("${app.upload.allowed-extensions}")
    private String allowedExtensions;

    // === 프로필 이미지 전용 설정 ===
    @Value("${app.profile-image.allowed-extensions}")
    private String profileImageAllowedExtensions;

    @Value("${app.profile-image.allowed-mime-types}")
    private String profileImageAllowedMimeTypes;

    @Value("${app.profile-image.max-size}")
    private long profileImageMaxSize;

    @Value("${app.file.base-url}")
    private String baseUrl;

    public List<PostAttachResponseDto> getPostAttachments(Long postId) {
        List<PostAttach> attachments = postAttachRepository.findByPostIdWithFileInfo(postId);
        return attachments.stream()
                .map(PostAttachResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public PostAttachResponseDto uploadPostAttachment(Long postId, MultipartFile file, Long accountId) {
        log.info("파일 업로드 요청 - postId: {}, fileName: {}, accountId: {}",
                postId, file.getOriginalFilename(), accountId);

        // 파일 유효성 검사
        validateFile(file);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        validatePostOwner(post, accountId);

        try {
            // 업로드 디렉토리 생성
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 파일 정보 생성
            String originalName = file.getOriginalFilename();
            String hashedName = generateHashedFileName(originalName);
            Long fileSize = file.getSize();

            // 파일 저장
            Path filePath = uploadPath.resolve(hashedName);
            Files.copy(file.getInputStream(), filePath);

            // FileInfo 엔티티 생성 및 저장
            FileInfo fileInfo = FileInfo.builder()
                    .nameOrigin(originalName)
                    .nameHashed(hashedName)
                    .size(fileSize)
                    .build();

            fileInfo = fileInfoRepository.save(fileInfo);

            // PostAttach 엔티티 생성 및 저장
            PostAttach postAttach = PostAttach.builder()
                    .post(post)
                    .fileInfo(fileInfo)
                    .build();

            postAttach = postAttachRepository.save(postAttach);

            log.info("파일 업로드 완료 - attachId: {}, filePath: {}", postAttach.getId(), filePath);

            return PostAttachResponseDto.from(postAttach);

        } catch (IOException e) {
            log.error("파일 업로드 실패 - postId: {}, fileName: {}", postId, file.getOriginalFilename(), e);
            throw new RuntimeException("파일 업로드에 실패했습니다.", e);
        }
    }

    @Transactional
    public void deletePostAttachment(Long attachId, Long accountId) {
        log.info("파일 삭제 요청 - attachId: {}, accountId: {}", attachId, accountId);

        PostAttach postAttach = postAttachRepository.findByIdWithFileInfo(attachId)
                .orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다."));

        validatePostOwner(postAttach.getPost(), accountId);

        deleteFileAndRecord(postAttach, attachId);
    }

    @Transactional
    public void deleteUserPostAttachment(Long attachId) {
        log.info("관리자 파일 삭제 요청 - attachId: {}", attachId);

        PostAttach postAttach = postAttachRepository.findByIdWithFileInfo(attachId)
                .orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다."));

        deleteFileAndRecord(postAttach, attachId);
    }

    private void deleteFileAndRecord(PostAttach postAttach, Long attachId) {
        try {
            // 실제 파일 삭제
            String hashedName = postAttach.getFileInfo().getNameHashed();
            Path filePath = Paths.get(uploadDir).resolve(hashedName);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("실제 파일 삭제 완료 - filePath: {}", filePath);
            }

            // 데이터베이스에서 삭제 (Soft Delete)
            postAttachRepository.delete(postAttach);

            log.info("첨부파일 삭제 완료 - attachId: {}", attachId);

        } catch (IOException e) {
            log.error("파일 삭제 실패 - attachId: {}", attachId, e);
            throw new RuntimeException("파일 삭제에 실패했습니다.", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        // 파일 크기 검증 (환경변수에서 설정된 값 사용)
        long maxSizeBytes = parseFileSize(maxFileSize);
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException(
                    String.format("파일 크기가 너무 큽니다. 최대 %s까지 허용됩니다.", maxFileSize));
        }

        // 파일 확장자 검증
        String fileName = file.getOriginalFilename();
        if (fileName == null || !isAllowedFileExtension(fileName)) {
            throw new IllegalArgumentException(
                    String.format("허용되지 않는 파일 형식입니다. 허용 형식: %s", allowedExtensions));
        }
    }

    private void validatePostOwner(Post post, Long accountId) {
        if (!post.getAccount().getId().equals(accountId)) {
            throw new IllegalArgumentException("게시글 작성자만 첨부파일을 관리할 수 있습니다.");
        }
    }

    private String generateHashedFileName(String originalName) {
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    private boolean isAllowedFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return false;
        }

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        Set<String> allowedExtSet = Arrays.stream(allowedExtensions.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return allowedExtSet.contains(extension);
    }

    private long parseFileSize(String fileSize) {
        if (fileSize == null || fileSize.trim().isEmpty()) {
            return 10 * 1024 * 1024; // 기본값 10MB
        }

        fileSize = fileSize.trim().toUpperCase();

        if (fileSize.endsWith("KB")) {
            return Long.parseLong(fileSize.replace("KB", "")) * 1024;
        } else if (fileSize.endsWith("MB")) {
            return Long.parseLong(fileSize.replace("MB", "")) * 1024 * 1024;
        } else if (fileSize.endsWith("GB")) {
            return Long.parseLong(fileSize.replace("GB", "")) * 1024 * 1024 * 1024;
        } else {
            return Long.parseLong(fileSize); // 바이트 단위
        }
    }

    // === 새로 추가할 프로필 이미지 기능 ===

    /**
     * 프로필 이미지 업로드
     * - 기존 프로필 이미지가 있으면 삭제 후 새 이미지 업로드
     * - Account.profileImageUrl 필드에 저장
     * 
     * @param file   업로드할 이미지 파일
     * @param userId 업로드하는 사용자 ID
     * @return 업로드된 파일의 접근 URL
     */
    @Transactional
    public String uploadProfileImage(MultipartFile file, Long userId) {
        log.info("프로필 이미지 업로드 시작: userId={}, fileSize={}", userId, file.getSize());

        // 1. 파일 유효성 검증
        if (!isValidProfileImage(file)) {
            throw new ValidationException(FileUploadErrorMessages.UNSUPPORTED_FILE_FORMAT);
        }

        // 2. 파일 크기 검증 (프로필 이미지 전용 크기 사용)
        if (file.getSize() > profileImageMaxSize) {
            long maxSizeMB = profileImageMaxSize / 1024 / 1024;
            throw new ValidationException(String.format(FileUploadErrorMessages.FILE_SIZE_EXCEEDED, maxSizeMB));
        }

        // 3. 사용자 조회
        Account account = accountRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        try {
            // 4. 기존 프로필 이미지가 있으면 삭제
            String existingImageUrl = account.getProfileImageUrl();
            if (existingImageUrl != null && !existingImageUrl.trim().isEmpty()) {
                deleteExistingProfileImageFile(existingImageUrl);
            }

            // 5. 업로드 디렉토리 생성 및 보안 검증
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 6. 원본 파일명 보안 검증
            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null || originalFileName.trim().isEmpty()) {
                throw new ValidationException(FileUploadErrorMessages.INVALID_FILE_NAME);
            }

            // 7. 보안: 원본 파일명 정제
            String sanitizedOriginalName = sanitizeFileName(originalFileName);
            String fileExtension = getFileExtension(sanitizedOriginalName);

            // 8. 고유하고 안전한 파일명 생성
            String fileName = "profile_" + userId + "_" + UUID.randomUUID().toString() + fileExtension;

            // 9. 보안: 생성된 파일명도 검증
            if (!isSecureFileName(fileName)) {
                throw new ValidationException(FileUploadErrorMessages.INSECURE_FILE_NAME);
            }

            // 10. 파일 저장 (경로 보안 검증)
            Path filePath = uploadPath.resolve(fileName);

            // 11. 보안: 파일 경로가 업로드 디렉토리 내부인지 확인
            if (!filePath.normalize().startsWith(uploadPath.normalize())) {
                throw new ValidationException(FileUploadErrorMessages.INVALID_FILE_PATH);
            }

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 12. FileInfo 엔티티 생성 및 저장
            FileInfo fileInfo = FileInfo.builder()
                    .nameOrigin(originalFileName)
                    .nameHashed(fileName)
                    .size(file.getSize())
                    .build();

            fileInfo = fileInfoRepository.save(fileInfo);

            // 13. 접근 URL 생성 및 Account 업데이트
            String fileUrl = baseUrl + "/uploads/" + fileName;
            account.setProfileImageUrl(fileUrl);
            accountRepository.save(account);

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

    /**
     * 프로필 이미지 삭제
     * - 파일 시스템에서 파일 삭제
     * - Account.profileImageUrl 필드를 null로 설정
     * 
     * @param userId 삭제 요청하는 사용자 ID
     */
    @Transactional
    public void deleteProfileImage(Long userId) {
        log.info("프로필 이미지 삭제 시작: userId={}", userId);

        // 1. 사용자 조회
        Account account = accountRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String imageUrl = account.getProfileImageUrl();
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            log.info("삭제할 프로필 이미지가 없음: userId={}", userId);
            return;
        }

        try {
            // 2. 실제 파일 삭제
            deleteExistingProfileImageFile(imageUrl);

            // 3. Account 엔티티의 profileImageUrl 필드를 null로 설정
            account.setProfileImageUrl(null);
            accountRepository.save(account);

            log.info("프로필 이미지 삭제 완료: userId={}", userId);

        } catch (Exception e) {
            log.error("프로필 이미지 삭제 실패: userId={}, error={}", userId, e.getMessage(), e);
            // 파일 삭제 실패는 치명적이지 않으므로 예외를 던지지 않고 Account만 업데이트
            account.setProfileImageUrl(null);
            accountRepository.save(account);
        }
    }

    /**
     * 프로필 이미지 파일 유효성 검증
     * - 파일 크기, 확장자, MIME 타입 검증
     * 
     * @param file 검증할 파일
     * @return 유효한 이미지 파일이면 true
     */
    private boolean isValidProfileImage(MultipartFile file) {
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
        String[] allowedExtensions = profileImageAllowedExtensions.split(",");
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
        String[] allowedMimeTypes = profileImageAllowedMimeTypes.split(",");
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
     * 기존 프로필 이미지 파일 삭제
     * 
     * @param imageUrl 삭제할 이미지 URL
     */
    private void deleteExistingProfileImageFile(String imageUrl) {
        try {
            // URL에서 파일명 추출
            String fileName = extractFileNameFromUrl(imageUrl);
            if (fileName == null) {
                log.warn("유효하지 않은 이미지 URL 형식: {}", imageUrl);
                return;
            }

            // 파일 경로 생성
            Path filePath = Paths.get(uploadDir).resolve(fileName);

            // 파일 존재 여부 확인 후 삭제
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("기존 프로필 이미지 파일 삭제 완료: {}", filePath);
            } else {
                log.warn("삭제할 파일이 존재하지 않음: {}", filePath);
            }

        } catch (IOException e) {
            log.error("프로필 이미지 파일 삭제 실패: {}, error={}", imageUrl, e.getMessage(), e);
            // 파일 삭제 실패는 치명적이지 않으므로 예외를 던지지 않음
        }
    }

    /**
     * URL에서 파일명 추출
     * 
     * @param url 파일 URL
     * @return 추출된 파일명 (실패 시 null)
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
     * 
     * @param fileName 검증할 파일명
     * @return 안전한 파일명이면 true
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
     * 
     * @param originalFileName 원본 파일명
     * @return 정제된 안전한 파일명
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

    /**
     * 파일명에서 확장자 추출
     * 
     * @param fileName 파일명
     * @return 확장자 (점 포함)
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex);
    }
}