package com.pickteam.util;

import lombok.extern.slf4j.Slf4j;

/**
 * 파일 관련 작업의 로깅을 통합 관리하는 유틸리티 클래스
 * 중복된 로깅 코드를 줄이고 일관된 로그 형식을 제공합니다.
 */
@Slf4j
public class FileOperationLogger {

    /**
     * 파일 작업 유형을 정의하는 열거형
     */
    public enum FileOperationType {
        // 게시글 첨부파일 관련
        POST_ATTACHMENT_UPLOAD("파일 업로드"),
        POST_ATTACHMENT_DELETE("파일 삭제"),
        POST_ATTACHMENT_ADMIN_DELETE("관리자 파일 삭제"),

        // 프로필 이미지 관련
        PROFILE_IMAGE_UPLOAD("프로필 이미지 업로드"),
        PROFILE_IMAGE_DELETE("프로필 이미지 삭제"),
        PROFILE_IMAGE_SOFT_DELETE("프로필 이미지 Soft Delete"),
        PROFILE_IMAGE_PHYSICAL_DELETE("프로필 이미지 물리적 파일 삭제"),

        // 공통 파일 관련
        FILE_VALIDATION("파일 검증"),
        FILE_PHYSICAL_DELETE("물리적 파일 삭제"),
        FILE_DOWNLOAD("파일 다운로드");

        private final String description;

        FileOperationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 로그 레벨을 정의하는 열거형
     */
    public enum LogLevel {
        INFO, WARN, ERROR, DEBUG
    }

    /**
     * 작업 시작 로그를 기록합니다.
     *
     * @param operationType 작업 유형
     * @param message       추가 메시지 (파라미터 정보 등)
     */
    public static void logOperationStart(FileOperationType operationType, String message) {
        log.info("{} 시작 - {}", operationType.getDescription(), message);
    }

    /**
     * 작업 완료 로그를 기록합니다.
     *
     * @param operationType 작업 유형
     * @param message       추가 메시지 (결과 정보 등)
     */
    public static void logOperationSuccess(FileOperationType operationType, String message) {
        log.info("{} 완료 - {}", operationType.getDescription(), message);
    }

    /**
     * 작업 실패 로그를 기록합니다.
     *
     * @param operationType 작업 유형
     * @param message       추가 메시지 (실패 정보 등)
     * @param throwable     예외 객체
     */
    public static void logOperationFailure(FileOperationType operationType, String message, Throwable throwable) {
        log.error("{} 실패 - {}", operationType.getDescription(), message, throwable);
    }

    /**
     * 작업 경고 로그를 기록합니다.
     *
     * @param operationType 작업 유형
     * @param message       경고 메시지
     */
    public static void logOperationWarning(FileOperationType operationType, String message) {
        log.warn("{} 경고 - {}", operationType.getDescription(), message);
    }

    /**
     * 작업 디버그 로그를 기록합니다.
     *
     * @param operationType 작업 유형
     * @param message       디버그 메시지
     */
    public static void logOperationDebug(FileOperationType operationType, String message) {
        log.debug("{} 디버그 - {}", operationType.getDescription(), message);
    }

    /**
     * 커스텀 레벨 로그를 기록합니다.
     *
     * @param level         로그 레벨
     * @param operationType 작업 유형
     * @param message       메시지
     */
    public static void logOperation(LogLevel level, FileOperationType operationType, String message) {
        String logMessage = "{} - {}";
        switch (level) {
            case INFO:
                log.info(logMessage, operationType.getDescription(), message);
                break;
            case WARN:
                log.warn(logMessage, operationType.getDescription(), message);
                break;
            case ERROR:
                log.error(logMessage, operationType.getDescription(), message);
                break;
            case DEBUG:
                log.debug(logMessage, operationType.getDescription(), message);
                break;
        }
    }

    /**
     * 파일 업로드 관련 파라미터 문자열을 생성합니다.
     *
     * @param postId   게시글 ID (nullable)
     * @param userId   사용자 ID (nullable)
     * @param fileName 파일명
     * @param fileSize 파일 크기 (nullable)
     * @return 포맷된 파라미터 문자열
     */
    public static String formatUploadParams(Long postId, Long userId, String fileName, Long fileSize) {
        StringBuilder sb = new StringBuilder();

        if (postId != null) {
            sb.append("postId: ").append(postId).append(", ");
        }
        if (userId != null) {
            sb.append("userId: ").append(userId).append(", ");
        }
        if (fileName != null) {
            sb.append("fileName: ").append(fileName);
        }
        if (fileSize != null) {
            sb.append(", fileSize: ").append(fileSize).append(" bytes");
        }

        return sb.toString();
    }

    /**
     * 파일 삭제 관련 파라미터 문자열을 생성합니다.
     *
     * @param fileId         파일 ID (nullable)
     * @param attachId       첨부파일 ID (nullable)
     * @param userId         사용자 ID (nullable)
     * @param hashedFileName 해시된 파일명 (nullable)
     * @return 포맷된 파라미터 문자열
     */
    public static String formatDeleteParams(Long fileId, Long attachId, Long userId, String hashedFileName) {
        StringBuilder sb = new StringBuilder();

        if (fileId != null) {
            sb.append("fileId: ").append(fileId).append(", ");
        }
        if (attachId != null) {
            sb.append("attachId: ").append(attachId).append(", ");
        }
        if (userId != null) {
            sb.append("userId: ").append(userId);
        }
        if (hashedFileName != null) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append("hashedFileName: ").append(hashedFileName);
        }

        return sb.toString();
    }

    /**
     * 파일 경로 정보 문자열을 생성합니다.
     *
     * @param filePath 파일 경로
     * @return 포맷된 경로 문자열
     */
    public static String formatFilePath(String filePath) {
        return "filePath: " + filePath;
    }

    /**
     * 파일 검증 실패 정보 문자열을 생성합니다.
     *
     * @param userId    사용자 ID
     * @param fileName  파일명
     * @param mimeType  MIME 타입 (nullable)
     * @param extension 확장자 (nullable)
     * @return 포맷된 검증 실패 정보 문자열
     */
    public static String formatValidationFailure(Long userId, String fileName, String mimeType, String extension) {
        StringBuilder sb = new StringBuilder();
        sb.append("userId: ").append(userId);

        if (fileName != null) {
            sb.append(", fileName: ").append(fileName);
        }
        if (mimeType != null) {
            sb.append(", mimeType: ").append(mimeType);
        }
        if (extension != null) {
            sb.append(", extension: ").append(extension);
        }

        return sb.toString();
    }

    /**
     * 보안 위험 탐지 정보 문자열을 생성합니다.
     *
     * @param userId   사용자 ID
     * @param fileName 파일명
     * @param riskType 위험 유형
     * @return 포맷된 보안 위험 정보 문자열
     */
    public static String formatSecurityRisk(Long userId, String fileName, String riskType) {
        return String.format("userId: %d, fileName: %s, riskType: %s", userId, fileName, riskType);
    }
}
