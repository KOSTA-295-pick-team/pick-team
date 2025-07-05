package com.pickteam.service.board;

import com.pickteam.domain.board.Post;
import com.pickteam.domain.board.PostAttach;
import com.pickteam.domain.common.FileInfo;
import com.pickteam.dto.board.PostAttachResponseDto;
import com.pickteam.repository.board.PostAttachRepository;
import com.pickteam.repository.board.PostRepository;
import com.pickteam.repository.common.FileInfoRepository;
import com.pickteam.util.FileSignatureValidator;
import com.pickteam.util.FileOperationLogger;
import com.pickteam.util.FileOperationLogger.FileOperationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.max-file-size}")
    private String maxFileSize;

    @Value("${app.upload.allowed-extensions}")
    private String allowedExtensions;

    // 프로필 이미지 관련 설정 추가
    @Value("${app.profile.image-dir}")
    private String profileImageDir;

    @Value("${app.profile.image.max-file-size}")
    private String profileMaxFileSize;

    @Value("${app.profile.image.allowed-extensions}")
    private String profileAllowedExtensions;

    @Value("${app.profile.image.allowed-mime-types}")
    private String profileAllowedMimeTypes;

    // ==================== 게시글 첨부파일 관리 ====================

    public List<PostAttachResponseDto> getPostAttachments(Long postId) {
        List<PostAttach> attachments = postAttachRepository.findByPostIdWithFileInfoAndIsDeletedFalse(postId);
        return attachments.stream()
                .map(PostAttachResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public PostAttachResponseDto uploadPostAttachment(Long postId, MultipartFile file, Long accountId) {
        FileOperationLogger.logOperationStart(FileOperationType.POST_ATTACHMENT_UPLOAD,
                FileOperationLogger.formatUploadParams(postId, accountId, file.getOriginalFilename(), file.getSize()));

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

            FileOperationLogger.logOperationSuccess(FileOperationType.POST_ATTACHMENT_UPLOAD,
                    "attachId: " + postAttach.getId() + ", " + FileOperationLogger.formatFilePath(filePath.toString()));

            return PostAttachResponseDto.from(postAttach);

        } catch (IOException e) {
            FileOperationLogger.logOperationFailure(FileOperationType.POST_ATTACHMENT_UPLOAD,
                    FileOperationLogger.formatUploadParams(postId, accountId, file.getOriginalFilename(), null), e);
            throw new RuntimeException("파일 업로드에 실패했습니다.", e);
        }
    }

    @Transactional
    public void deletePostAttachment(Long attachId, Long accountId) {
        FileOperationLogger.logOperationStart(FileOperationType.POST_ATTACHMENT_DELETE,
                FileOperationLogger.formatDeleteParams(null, attachId, accountId, null));

        PostAttach postAttach = postAttachRepository.findByIdWithFileInfoAndIsDeletedFalse(attachId)
                .orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다."));

        validatePostOwner(postAttach.getPost(), accountId);

        deleteFileAndRecord(postAttach, attachId);
    }

    @Transactional
    public void deleteUserPostAttachment(Long attachId) {
        FileOperationLogger.logOperationStart(FileOperationType.POST_ATTACHMENT_ADMIN_DELETE,
                "attachId: " + attachId);

        PostAttach postAttach = postAttachRepository.findByIdWithFileInfoAndIsDeletedFalse(attachId)
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
                FileOperationLogger.logOperationSuccess(FileOperationType.FILE_PHYSICAL_DELETE,
                        FileOperationLogger.formatFilePath(filePath.toString()));
            }

            // 수동 Soft Delete
            postAttach.markDeleted();

            FileOperationLogger.logOperationSuccess(FileOperationType.POST_ATTACHMENT_DELETE,
                    "attachId: " + attachId);

        } catch (IOException e) {
            FileOperationLogger.logOperationFailure(FileOperationType.POST_ATTACHMENT_DELETE,
                    "attachId: " + attachId, e);
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

    // ==================== 프로필 이미지 관리 (보안 강화) ====================

    /**
     * 프로필 이미지 업로드 및 기존 이미지 교체 (트랜잭션 안전성 보장)
     *
     * @param file        업로드할 이미지 파일
     * @param userId      사용자 ID
     * @param oldImageUrl 기존 이미지 URL (있다면)
     * @return 저장된 FileInfo 엔티티
     */
    @Transactional
    public FileInfo uploadProfileImageWithReplace(MultipartFile file, Long userId, String oldImageUrl) {
        FileOperationLogger.logOperationStart(FileOperationType.PROFILE_IMAGE_UPLOAD,
                FileOperationLogger.formatUploadParams(null, userId, file.getOriginalFilename(), file.getSize())
                        + ", oldImageUrl: " + oldImageUrl);

        // 1. 프로필 이미지 보안 검증
        validateProfileImageFile(file, userId);

        try {
            // 2. 새 파일 업로드 먼저 (안전)
            FileInfo newFileInfo = uploadProfileImageFile(file, userId);
            String newHashedFileName = newFileInfo.getNameHashed();

            // 3. 기존 파일 정보 추출 및 Soft Delete
            String oldHashedFileName = null;
            if (oldImageUrl != null && !oldImageUrl.trim().isEmpty()) {
                oldHashedFileName = extractFileNameFromUrl(oldImageUrl);
                // DB에서만 Soft Delete 수행 (물리적 파일은 나중에 삭제)
                softDeleteProfileImageByFileName(oldHashedFileName, userId);
            }

            // 4. 트랜잭션 동기화 등록 (커밋/롤백 시 물리적 파일 정리)
            if (oldHashedFileName != null) {
                final String finalOldFileName = oldHashedFileName;
                TransactionSynchronizationManager.registerSynchronization(
                        new TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                // 커밋 성공 시: 기존 파일 물리적 삭제
                                try {
                                    deletePhysicalProfileImageFile(finalOldFileName);
                                    FileOperationLogger.logOperationSuccess(
                                            FileOperationType.PROFILE_IMAGE_PHYSICAL_DELETE,
                                            "기존 파일 삭제 (커밋 후): " + finalOldFileName);
                                } catch (Exception e) {
                                    FileOperationLogger.logOperationFailure(
                                            FileOperationType.PROFILE_IMAGE_PHYSICAL_DELETE,
                                            "기존 파일 삭제 실패 (커밋 후): " + finalOldFileName, e);
                                }
                            }

                            @Override
                            public void afterCompletion(int status) {
                                if (status == STATUS_ROLLED_BACK) {
                                    // 롤백 시: 새로 업로드된 파일 물리적 삭제
                                    try {
                                        deletePhysicalProfileImageFile(newHashedFileName);
                                        FileOperationLogger.logOperationWarning(
                                                FileOperationType.PROFILE_IMAGE_PHYSICAL_DELETE,
                                                "트랜잭션 롤백으로 새 파일 삭제: " + newHashedFileName);
                                    } catch (Exception e) {
                                        FileOperationLogger.logOperationFailure(
                                                FileOperationType.PROFILE_IMAGE_PHYSICAL_DELETE,
                                                "새 파일 정리 실패 (롤백 후): " + newHashedFileName, e);
                                    }
                                }
                            }
                        });
            } else {
                // 기존 파일이 없는 경우, 롤백 시에만 새 파일 정리
                TransactionSynchronizationManager.registerSynchronization(
                        new TransactionSynchronization() {
                            @Override
                            public void afterCompletion(int status) {
                                if (status == STATUS_ROLLED_BACK) {
                                    try {
                                        deletePhysicalProfileImageFile(newHashedFileName);
                                        FileOperationLogger.logOperationWarning(
                                                FileOperationType.PROFILE_IMAGE_PHYSICAL_DELETE,
                                                "트랜잭션 롤백으로 새 파일 삭제: " + newHashedFileName);
                                    } catch (Exception e) {
                                        FileOperationLogger.logOperationFailure(
                                                FileOperationType.PROFILE_IMAGE_PHYSICAL_DELETE,
                                                "새 파일 정리 실패 (롤백 후): " + newHashedFileName, e);
                                    }
                                }
                            }
                        });
            }

            FileOperationLogger.logOperationSuccess(FileOperationType.PROFILE_IMAGE_UPLOAD,
                    "userId: " + userId + ", newFileId: " + newFileInfo.getId());
            return newFileInfo;

        } catch (Exception e) {
            FileOperationLogger.logOperationFailure(FileOperationType.PROFILE_IMAGE_UPLOAD,
                    "userId: " + userId, e);
            throw new RuntimeException("프로필 이미지 업로드 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 해시된 파일명으로 프로필 이미지 삭제
     *
     * @param hashedFileName 해시된 파일명
     * @param userId         사용자 ID (로깅용)
     */
    @Transactional
    public void deleteProfileImageByFileName(String hashedFileName, Long userId) {
        FileOperationLogger.logOperationStart(FileOperationType.PROFILE_IMAGE_DELETE,
                FileOperationLogger.formatDeleteParams(null, null, userId, hashedFileName));

        try {
            // 1. FileInfo 조회
            Optional<FileInfo> fileInfoOpt = fileInfoRepository.findByNameHashedAndIsDeletedFalse(hashedFileName);

            if (fileInfoOpt.isPresent()) {
                FileInfo fileInfo = fileInfoOpt.get();

                // 2. 물리적 파일 삭제
                deletePhysicalProfileImageFile(fileInfo.getNameHashed());

                // 3. FileInfo soft delete
                fileInfo.markDeleted();

                FileOperationLogger.logOperationSuccess(FileOperationType.PROFILE_IMAGE_DELETE,
                        FileOperationLogger.formatDeleteParams(null, null, userId, hashedFileName));
            } else {
                FileOperationLogger.logOperationWarning(FileOperationType.PROFILE_IMAGE_DELETE,
                        "삭제할 파일을 찾을 수 없음 - "
                                + FileOperationLogger.formatDeleteParams(null, null, userId, hashedFileName));
                // 파일이 없어도 예외를 던지지 않음 (이미 삭제된 상태로 간주)
            }
        } catch (Exception e) {
            FileOperationLogger.logOperationFailure(FileOperationType.PROFILE_IMAGE_DELETE,
                    FileOperationLogger.formatDeleteParams(null, null, userId, hashedFileName), e);
            if (e instanceof SecurityException) {
                throw e; // 보안 예외는 그대로 전파
            }
            throw new RuntimeException("프로필 이미지 삭제 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * URL에서 파일명 추출하는 헬퍼 메서드
     *
     * @param imageUrl 이미지 URL (예: "/profile-images/uuid-filename.jpg")
     * @return 파일명 (예: "uuid-filename.jpg")
     */
    public String extractFileNameFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return "";
        }
        return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    }

    /**
     * 프로필 이미지 URL 생성
     *
     * @param hashedFileName 해시된 파일명
     * @return 웹에서 접근 가능한 이미지 URL
     */
    public String generateProfileImageUrl(String hashedFileName) {
        return "/profile-images/" + hashedFileName;
    }

    // ==================== 프로필 이미지 내부 유틸리티 메서드 ====================

    /**
     * 실제 프로필 이미지 파일 업로드
     */
    private FileInfo uploadProfileImageFile(MultipartFile file, Long userId) throws IOException {
        // 프로필 이미지 디렉토리 생성
        Path uploadPath = Paths.get(profileImageDir);
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

        FileOperationLogger.logOperationSuccess(FileOperationType.PROFILE_IMAGE_UPLOAD,
                "파일 업로드 완료 - userId: " + userId + ", fileId: " + fileInfo.getId() + ", " +
                        FileOperationLogger.formatFilePath(filePath.toString()));

        return fileInfo;
    }

    /**
     * 프로필 이미지 파일 보안 검증
     */
    private void validateProfileImageFile(MultipartFile file, Long userId) {
        if (file.isEmpty()) {
            FileOperationLogger.logOperationWarning(FileOperationType.FILE_VALIDATION,
                    "빈 파일 업로드 시도 - userId: " + userId);
            throw new IllegalArgumentException("프로필 이미지 파일이 비어있습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            FileOperationLogger.logOperationWarning(FileOperationType.FILE_VALIDATION,
                    "유효하지 않은 파일명 - userId: " + userId);
            throw new IllegalArgumentException("파일명이 유효하지 않습니다.");
        }

        // 파일 크기 검증 (프로필 이미지 전용 크기 제한)
        long maxSizeBytes = parseFileSize(profileMaxFileSize);
        if (file.getSize() > maxSizeBytes) {
            FileOperationLogger.logOperationWarning(FileOperationType.FILE_VALIDATION,
                    "파일 크기 초과 - userId: " + userId + ", size: " + file.getSize() + ", limit: " + maxSizeBytes);
            throw new IllegalArgumentException(
                    String.format("프로필 이미지 크기가 너무 큽니다. 최대 %s까지 허용됩니다.", profileMaxFileSize));
        }

        // 확장자 검증 (프로필 이미지 전용 확장자)
        if (!isAllowedProfileImageExtension(originalFilename)) {
            FileOperationLogger.logOperationWarning(FileOperationType.FILE_VALIDATION,
                    FileOperationLogger.formatValidationFailure(userId, originalFilename, null,
                            getFileExtension(originalFilename)));
            throw new IllegalArgumentException(
                    String.format("허용되지 않는 이미지 형식입니다. 허용 형식: %s", profileAllowedExtensions));
        }

        // MIME 타입 검증 (브라우저에서 제공하는 Content-Type)
        String contentType = file.getContentType();
        if (!isAllowedProfileImageMimeType(contentType)) {
            FileOperationLogger.logOperationWarning(FileOperationType.FILE_VALIDATION,
                    FileOperationLogger.formatValidationFailure(userId, originalFilename, contentType, null));
            throw new IllegalArgumentException(
                    String.format("허용되지 않는 이미지 형식입니다. 허용 MIME 타입: %s", profileAllowedMimeTypes));
        }

        // 파일 시그니처 검증 (실제 파일 헤더 검사)
        String extension = getFileExtension(originalFilename);
        if (!FileSignatureValidator.validateFileSignature(file, extension)) {
            FileOperationLogger.logOperationWarning(FileOperationType.FILE_VALIDATION,
                    FileOperationLogger.formatValidationFailure(userId, originalFilename, null, extension)
                            + " (시그니처 검증 실패)");
            throw new SecurityException("파일의 실제 형식이 확장자와 일치하지 않습니다. 보안상 업로드가 거부됩니다.");
        }

        // 파일명 보안 검증 (경로 탐색 공격 방지)
        validateSecureFileName(originalFilename, userId);

        FileOperationLogger.logOperationDebug(FileOperationType.FILE_VALIDATION,
                "프로필 이미지 파일 보안 검증 통과 - userId: " + userId + ", filename: " + originalFilename);
    }

    /**
     * 프로필 이미지 허용 확장자 검증
     */
    private boolean isAllowedProfileImageExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return false;
        }

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        Set<String> allowedExtSet = Arrays.stream(profileAllowedExtensions.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return allowedExtSet.contains(extension);
    }

    /**
     * 프로필 이미지 허용 MIME 타입 검증
     */
    private boolean isAllowedProfileImageMimeType(String mimeType) {
        if (mimeType == null || mimeType.trim().isEmpty()) {
            return false;
        }

        Set<String> allowedMimeSet = Arrays.stream(profileAllowedMimeTypes.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return allowedMimeSet.contains(mimeType.toLowerCase());
    }

    /**
     * 파일명에서 확장자 추출
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 파일명 보안 검증 (경로 탐색 공격 방지)
     */
    private void validateSecureFileName(String fileName, Long userId) {
        // 경로 탐색 공격 패턴 검사
        if (fileName.contains("../") || fileName.contains("..\\") ||
                fileName.contains("/") || fileName.contains("\\")) {
            FileOperationLogger.logOperationWarning(FileOperationType.FILE_VALIDATION,
                    FileOperationLogger.formatSecurityRisk(userId, fileName, "경로 탐색 공격 시도"));
            throw new SecurityException("허용되지 않는 파일명입니다.");
        }

        // 파일명 길이 검사
        if (fileName.length() > 255) {
            FileOperationLogger.logOperationWarning(FileOperationType.FILE_VALIDATION,
                    FileOperationLogger.formatSecurityRisk(userId, fileName, "파일명 길이 초과: " + fileName.length()));
            throw new IllegalArgumentException("파일명이 너무 깁니다.");
        }

        // 위험한 문자 검사
        if (fileName.matches(".*[<>:\"|?*].*")) {
            FileOperationLogger.logOperationWarning(FileOperationType.FILE_VALIDATION,
                    FileOperationLogger.formatSecurityRisk(userId, fileName, "위험한 문자 포함"));
            throw new IllegalArgumentException("파일명에 허용되지 않는 문자가 포함되어 있습니다.");
        }
    }

    /**
     * 물리적 프로필 이미지 파일 삭제
     */
    private void deletePhysicalProfileImageFile(String hashedFileName) {
        try {
            Path filePath = Paths.get(profileImageDir).resolve(hashedFileName);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                FileOperationLogger.logOperationSuccess(FileOperationType.PROFILE_IMAGE_PHYSICAL_DELETE,
                        "물리적 파일 삭제 완료: " + filePath);
            } else {
                FileOperationLogger.logOperationWarning(FileOperationType.PROFILE_IMAGE_PHYSICAL_DELETE,
                        "삭제할 물리적 파일이 존재하지 않음: " + filePath);
            }
        } catch (IOException e) {
            FileOperationLogger.logOperationFailure(FileOperationType.PROFILE_IMAGE_PHYSICAL_DELETE,
                    "hashedFileName: " + hashedFileName, e);
            throw new RuntimeException("프로필 이미지 파일 삭제에 실패했습니다.", e);
        }
    }

    /**
     * 프로필 이미지 Soft Delete만 수행 (물리적 파일은 삭제하지 않음)
     */
    @Transactional
    public void softDeleteProfileImageByFileName(String hashedFileName, Long userId) {
        FileOperationLogger.logOperationStart(FileOperationType.PROFILE_IMAGE_SOFT_DELETE,
                FileOperationLogger.formatDeleteParams(null, null, userId, hashedFileName));

        Optional<FileInfo> fileInfoOpt = fileInfoRepository.findByNameHashedAndIsDeletedFalse(hashedFileName);
        if (fileInfoOpt.isPresent()) {
            FileInfo fileInfo = fileInfoOpt.get();
            fileInfo.markDeleted();
            FileOperationLogger.logOperationSuccess(FileOperationType.PROFILE_IMAGE_SOFT_DELETE,
                    FileOperationLogger.formatDeleteParams(null, null, userId, hashedFileName));
        } else {
            FileOperationLogger.logOperationWarning(FileOperationType.PROFILE_IMAGE_SOFT_DELETE,
                    "Soft Delete할 파일을 찾을 수 없음 - "
                            + FileOperationLogger.formatDeleteParams(null, null, userId, hashedFileName));
        }
    }
}