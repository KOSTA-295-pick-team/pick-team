package com.pickteam.service.board;

import com.pickteam.domain.board.Post;
import com.pickteam.domain.board.PostAttach;
import com.pickteam.domain.common.FileInfo;
import com.pickteam.dto.board.PostAttachResponseDto;
import com.pickteam.repository.board.PostAttachRepository;
import com.pickteam.repository.board.PostRepository;
import com.pickteam.repository.common.FileInfoRepository;
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
    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.max-file-size}")
    private String maxFileSize;

    @Value("${app.upload.allowed-extensions}")
    private String allowedExtensions;

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
}