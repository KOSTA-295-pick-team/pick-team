package com.pickteam.constants;

/**
 * 파일 업로드 관련 에러 메시지 상수
 * - 파일 업로드 서비스에서 사용하는 에러 메시지들
 * - 하드코딩 방지 및 메시지 일관성 유지
 */
public class FileUploadErrorMessages {

    // 파일 형식 관련 에러
    public static final String UNSUPPORTED_FILE_FORMAT = "지원하지 않는 파일 형식입니다. JPG, PNG, GIF, WEBP 파일만 업로드 가능합니다.";

    // 파일 크기 관련 에러
    public static final String FILE_SIZE_EXCEEDED = "파일 크기가 너무 큽니다. 최대 %dMB까지 업로드 가능합니다.";

    // 파일 업로드 프로세스 에러
    public static final String FILE_UPLOAD_FAILED = "파일 업로드 중 오류가 발생했습니다.";

    // 파일 경로/URL 관련 에러
    public static final String INVALID_IMAGE_URL = "유효하지 않은 이미지 URL입니다.";

    // 파일 삭제 관련 에러
    public static final String FILE_NOT_FOUND_FOR_DELETE = "삭제할 파일이 존재하지 않습니다.";

    // 보안 관련 에러 (상세한 정보 노출 방지)
    public static final String INVALID_FILE_NAME = "파일명이 유효하지 않습니다.";
    public static final String INSECURE_FILE_NAME = "안전하지 않은 파일명이 감지되었습니다.";
    public static final String INVALID_FILE_PATH = "파일 경로가 허용된 범위를 벗어났습니다.";
    public static final String FILE_PROCESSING_ERROR = "파일 처리 중 오류가 발생했습니다.";

    private FileUploadErrorMessages() {
        // 상수 클래스이므로 인스턴스 생성 방지
    }
}
