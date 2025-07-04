package com.pickteam.service.board;

import com.pickteam.domain.board.Post;
import com.pickteam.domain.board.PostAttach;
import com.pickteam.domain.common.FileInfo;
import com.pickteam.dto.board.PostAttachResponseDto;
import com.pickteam.repository.board.PostAttachRepository;
import com.pickteam.repository.board.PostRepository;
import com.pickteam.repository.common.FileInfoRepository;
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

    public List<PostAttachResponseDto> getPostAttachments(Long postId) {
        List<PostAttach> attachments = postAttachRepository.findByPostIdWithFileInfoAndIsDeletedFalse(postId);
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

        PostAttach postAttach = postAttachRepository.findByIdWithFileInfoAndIsDeletedFalse(attachId)
                .orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다."));

        validatePostOwner(postAttach.getPost(), accountId);

        deleteFileAndRecord(postAttach, attachId);
    }

    @Transactional
    public void deleteUserPostAttachment(Long attachId) {
        log.info("관리자 파일 삭제 요청 - attachId: {}", attachId);

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
                log.info("실제 파일 삭제 완료 - filePath: {}", filePath);
            }

            // 수동 Soft Delete
            postAttach.markDeleted();

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

    // ==================== 프로필 이미지 관리 메서드들 ====================

    /**
     * 프로필 이미지 업로드
     * 
     * @param file   업로드할 이미지 파일
     * @param userId 사용자 ID
     * @return 저장된 FileInfo 엔티티
     */
    @Transactional
    public FileInfo uploadProfileImage(MultipartFile file, Long userId) {
        log.info("프로필 이미지 업로드 요청 - userId: {}, fileName: {}", userId, file.getOriginalFilename());

        // 프로필 이미지 전용 유효성 검사
        validateProfileImage(file);

        try {
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

            log.info("프로필 이미지 업로드 완료 - userId: {}, fileId: {}, filePath: {}",
                    userId, fileInfo.getId(), filePath);

            return fileInfo;

        } catch (IOException e) {
            log.error("프로필 이미지 업로드 실패 - userId: {}, fileName: {}", userId, file.getOriginalFilename(), e);
            throw new RuntimeException("프로필 이미지 업로드에 실패했습니다.", e);
        }
    }

    /**
     * 프로필 이미지 삭제 (FileInfo ID 기준)
     * 
     * @param fileInfoId 삭제할 FileInfo ID
     * @param userId     사용자 ID (로깅용)
     */
    @Transactional
    public void deleteProfileImage(Long fileInfoId, Long userId) {
        log.info("프로필 이미지 삭제 요청 - fileInfoId: {}, userId: {}", fileInfoId, userId);

        FileInfo fileInfo = fileInfoRepository.findById(fileInfoId)
                .orElseThrow(() -> new IllegalArgumentException("프로필 이미지 파일을 찾을 수 없습니다."));

        try {
            // 실제 파일 삭제
            String hashedName = fileInfo.getNameHashed();
            Path filePath = Paths.get(profileImageDir).resolve(hashedName);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("프로필 이미지 파일 삭제 완료 - filePath: {}", filePath);
            }

            // Soft Delete 처리
            fileInfo.markDeleted();

            log.info("프로필 이미지 삭제 완료 - fileInfoId: {}, userId: {}", fileInfoId, userId);

        } catch (IOException e) {
            log.error("프로필 이미지 파일 삭제 실패 - fileInfoId: {}, userId: {}", fileInfoId, userId, e);
            throw new RuntimeException("프로필 이미지 삭제에 실패했습니다.", e);
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
        log.info("프로필 이미지 파일 삭제 시작 - hashedFileName: {}, userId: {}", hashedFileName, userId);

        // 1. FileInfo 조회
        Optional<FileInfo> fileInfoOpt = fileInfoRepository.findByNameHashedAndIsDeletedFalse(hashedFileName);

        if (fileInfoOpt.isPresent()) {
            FileInfo fileInfo = fileInfoOpt.get();

            // 2. 물리적 파일 삭제
            deletePhysicalProfileImageFile(fileInfo.getNameHashed());

            // 3. FileInfo soft delete
            fileInfo.markDeleted();

            log.info("프로필 이미지 삭제 완료 - hashedFileName: {}, userId: {}", hashedFileName, userId);
        } else {
            log.warn("삭제할 프로필 이미지 파일을 찾을 수 없음 - hashedFileName: {}, userId: {}", hashedFileName, userId);
            // 파일이 없어도 예외를 던지지 않음 (이미 삭제된 상태로 간주)
        }
    }

    /**
     * 물리적 프로필 이미지 파일 삭제
     * 
     * @param hashedFileName 해시된 파일명
     */
    private void deletePhysicalProfileImageFile(String hashedFileName) {
        try {
            Path filePath = Paths.get(profileImageDir).resolve(hashedFileName);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("프로필 이미지 물리적 파일 삭제 완료: {}", filePath);
            } else {
                log.warn("삭제할 물리적 파일이 존재하지 않음: {}", filePath);
            }
        } catch (IOException e) {
            log.error("프로필 이미지 파일 삭제 실패 - hashedFileName: {}", hashedFileName, e);
            throw new RuntimeException("프로필 이미지 파일 삭제에 실패했습니다.", e);
        }
    }

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
        log.info("프로필 이미지 업로드 및 교체 시작 - userId: {}, oldImageUrl: {}", userId, oldImageUrl);

        // 1. 새 파일 업로드 먼저 (안전)
        FileInfo newFileInfo = uploadProfileImage(file, userId);
        String newHashedFileName = newFileInfo.getNameHashed();

        // 2. 기존 파일 정보 추출 및 Soft Delete
        String oldHashedFileName = null;
        if (oldImageUrl != null && !oldImageUrl.trim().isEmpty()) {
            oldHashedFileName = extractFileNameFromUrl(oldImageUrl);
            // DB에서만 Soft Delete 수행 (물리적 파일은 나중에 삭제)
            softDeleteProfileImageByFileName(oldHashedFileName, userId);
        }

        // 3. 트랜잭션 동기화 등록 (커밋/롤백 시 물리적 파일 정리)
        if (oldHashedFileName != null) {
            final String finalOldFileName = oldHashedFileName;
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            // 커밋 성공 시: 기존 파일 물리적 삭제
                            try {
                                deletePhysicalProfileImageFile(finalOldFileName);
                                log.info("트랜잭션 커밋 후 기존 파일 삭제 완료: {}", finalOldFileName);
                            } catch (Exception e) {
                                log.error("기존 파일 삭제 실패 (트랜잭션 커밋 후): {}", finalOldFileName, e);
                            }
                        }

                        @Override
                        public void afterCompletion(int status) {
                            if (status == STATUS_ROLLED_BACK) {
                                // 롤백 시: 새로 업로드된 파일 물리적 삭제
                                try {
                                    deletePhysicalProfileImageFile(newHashedFileName);
                                    log.warn("트랜잭션 롤백으로 새 파일 삭제: {}", newHashedFileName);
                                } catch (Exception e) {
                                    log.error("새 파일 정리 실패 (트랜잭션 롤백 후): {}", newHashedFileName, e);
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
                                    log.warn("트랜잭션 롤백으로 새 파일 삭제: {}", newHashedFileName);
                                } catch (Exception e) {
                                    log.error("새 파일 정리 실패 (트랜잭션 롤백 후): {}", newHashedFileName, e);
                                }
                            }
                        }
                    });
        }

        log.info("프로필 이미지 업로드 및 교체 완료 - userId: {}, newFileId: {}", userId, newFileInfo.getId());
        return newFileInfo;
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

    /**
     * 프로필 이미지 전용 유효성 검증
     * 
     * @param file 검증할 파일
     */
    private void validateProfileImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("프로필 이미지 파일이 비어있습니다.");
        }

        // 파일 크기 검증 (프로필 이미지 전용 크기 제한)
        long maxSizeBytes = parseFileSize(profileMaxFileSize);
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException(
                    String.format("프로필 이미지 크기가 너무 큽니다. 최대 %s까지 허용됩니다.", profileMaxFileSize));
        }

        // 파일 확장자 검증 (프로필 이미지 전용 확장자)
        String fileName = file.getOriginalFilename();
        if (fileName == null || !isAllowedProfileImageExtension(fileName)) {
            throw new IllegalArgumentException(
                    String.format("허용되지 않는 이미지 형식입니다. 허용 형식: %s", profileAllowedExtensions));
        }
    }

    /**
     * 프로필 이미지 허용 확장자 검증
     * 
     * @param fileName 검증할 파일명
     * @return 허용된 확장자인지 여부
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
     * 프로필 이미지 Soft Delete만 수행 (물리적 파일은 삭제하지 않음)
     * 
     * @param hashedFileName 해시된 파일명
     * @param userId         사용자 ID (로깅용)
     */
    @Transactional
    public void softDeleteProfileImageByFileName(String hashedFileName, Long userId) {
        log.info("프로필 이미지 Soft Delete 시작 - hashedFileName: {}, userId: {}", hashedFileName, userId);

        Optional<FileInfo> fileInfoOpt = fileInfoRepository.findByNameHashedAndIsDeletedFalse(hashedFileName);
        if (fileInfoOpt.isPresent()) {
            FileInfo fileInfo = fileInfoOpt.get();
            fileInfo.markDeleted();
            log.info("프로필 이미지 Soft Delete 완료 - hashedFileName: {}, userId: {}", hashedFileName, userId);
        } else {
            log.warn("Soft Delete할 프로필 이미지 파일을 찾을 수 없음 - hashedFileName: {}, userId: {}", hashedFileName, userId);
        }
    }
}