package com.pickteam.service.user;

import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 업로드 서비스 인터페이스
 * - 프로필 이미지 등 파일 업로드 관리
 * - 파일 저장, 삭제, URL 생성 기능 제공
 */
public interface FileUploadService {

    /**
     * 프로필 이미지 업로드
     * 
     * @param file   업로드할 이미지 파일
     * @param userId 업로드하는 사용자 ID
     * @return 업로드된 파일의 접근 URL
     */
    String uploadProfileImage(MultipartFile file, Long userId);

    /**
     * 프로필 이미지 삭제
     * 
     * @param imageUrl 삭제할 이미지 URL
     * @param userId   삭제 요청하는 사용자 ID
     */
    void deleteProfileImage(String imageUrl, Long userId);

    /**
     * 파일 유효성 검증
     * 
     * @param file 검증할 파일
     * @return 유효한 파일이면 true
     */
    boolean isValidImageFile(MultipartFile file);
}
