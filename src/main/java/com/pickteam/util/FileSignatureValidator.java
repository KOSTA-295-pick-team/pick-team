package com.pickteam.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 파일 시그니처(매직 넘버) 검증 유틸리티
 * - 파일 확장자 속임 공격 방지
 * - 실제 파일 타입과 확장자 일치 여부 검증
 */
@Slf4j
public class FileSignatureValidator {

    /**
     * 파일 확장자별 매직 넘버(시그니처) 맵
     * 각 확장자마다 가능한 시그니처들을 리스트로 관리
     */
    private static final Map<String, List<byte[]>> FILE_SIGNATURES;

    static {
        FILE_SIGNATURES = new HashMap<>();

        // 이미지 파일
        FILE_SIGNATURES.put("jpg", List.of(
                new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0 }, // JFIF
                new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE1 }, // EXIF
                new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xDB } // JPEG raw
        ));
        FILE_SIGNATURES.put("jpeg", List.of(
                new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0 },
                new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE1 },
                new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xDB }));
        FILE_SIGNATURES.put("png", List.of(
                new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A }));
        FILE_SIGNATURES.put("gif", List.of(
                new byte[] { 'G', 'I', 'F', '8', '7', 'a' }, // GIF87a
                new byte[] { 'G', 'I', 'F', '8', '9', 'a' } // GIF89a
        ));
        FILE_SIGNATURES.put("webp", List.of(
                // RIFF 헤더(4바이트) + 파일크기(4바이트, 가변) + WEBP 식별자(4바이트)
                // 8번째 바이트부터 'WEBP' 식별자를 확인하는 방식으로 별도 처리됨
                new byte[] { 'R', 'I', 'F', 'F' } // RIFF 시그니처 (WebP 특별 검증 로직 사용)
        ));

        // 문서 파일
        FILE_SIGNATURES.put("pdf", List.of(
                new byte[] { '%', 'P', 'D', 'F', '-' }));
        FILE_SIGNATURES.put("doc", List.of(
                new byte[] { (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A,
                        (byte) 0xE1 }));
        FILE_SIGNATURES.put("docx", List.of(
                new byte[] { 'P', 'K', 0x03, 0x04 } // ZIP 기반 (Office Open XML)
        ));
        FILE_SIGNATURES.put("xls", List.of(
                new byte[] { (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A,
                        (byte) 0xE1 }));
        FILE_SIGNATURES.put("xlsx", List.of(
                new byte[] { 'P', 'K', 0x03, 0x04 }));
        FILE_SIGNATURES.put("ppt", List.of(
                new byte[] { (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A,
                        (byte) 0xE1 }));
        FILE_SIGNATURES.put("pptx", List.of(
                new byte[] { 'P', 'K', 0x03, 0x04 }));

        // 압축 파일
        FILE_SIGNATURES.put("zip", List.of(
                new byte[] { 'P', 'K', 0x03, 0x04 }, // 로컬 파일 헤더
                new byte[] { 'P', 'K', 0x05, 0x06 } // 중앙 디렉토리 끝
        ));
        FILE_SIGNATURES.put("rar", List.of(
                new byte[] { 'R', 'a', 'r', '!', 0x1A, 0x07, 0x00 }, // RAR 4.x
                new byte[] { 'R', 'a', 'r', '!', 0x1A, 0x07, 0x01, 0x00 } // RAR 5.x
        ));
        FILE_SIGNATURES.put("7z", List.of(
                new byte[] { '7', 'z', (byte) 0xBC, (byte) 0xAF, 0x27, 0x1C }));

        // 텍스트 파일
        FILE_SIGNATURES.put("txt", List.of(
                // UTF-8 BOM
                new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF }
        // 참고: 일반 ASCII/UTF-8 텍스트는 시그니처가 없으므로 별도 검증 로직 필요
        ));
    }

    /**
     * 업로드된 파일의 시그니처가 확장자와 일치하는지 검증
     * 
     * @param file              검증할 파일
     * @param expectedExtension 예상 확장자 (소문자)
     * @return 시그니처가 일치하면 true, 그렇지 않으면 false
     */
    public static boolean validateFileSignature(MultipartFile file, String expectedExtension) {
        if (file == null || file.isEmpty()) {
            log.warn("파일 시그니처 검증 실패: 파일이 비어있음");
            return false;
        }

        expectedExtension = expectedExtension.toLowerCase();

        // 시그니처가 정의되지 않은 확장자는 true 반환 (기본 허용)
        List<byte[]> expectedSignatures = FILE_SIGNATURES.get(expectedExtension);
        if (expectedSignatures == null) {
            log.debug("파일 시그니처 정의되지 않음 - 확장자: {}, 기본 허용", expectedExtension);
            return true;
        }

        try {
            // 파일 헤더 읽기 (최대 16바이트)
            byte[] fileHeader = readFileHeader(file, 16);
            if (fileHeader == null || fileHeader.length == 0) {
                log.warn("파일 시그니처 검증 실패: 파일 헤더를 읽을 수 없음 - fileName: {}",
                        file.getOriginalFilename());
                return false;
            }

            // 모든 가능한 시그니처와 비교
            boolean matches = expectedSignatures.stream()
                    .anyMatch(signature -> matchesSignature(fileHeader, signature));

            if (!matches) {
                log.warn("파일 시그니처 불일치 - fileName: {}, expectedExtension: {}, fileHeaderHex: {}",
                        file.getOriginalFilename(), expectedExtension, bytesToHex(fileHeader));
            } else {
                log.debug("파일 시그니처 검증 성공 - fileName: {}, extension: {}",
                        file.getOriginalFilename(), expectedExtension);
            }

            return matches;

        } catch (Exception e) {
            log.error("파일 시그니처 검증 중 오류 발생 - fileName: {}, extension: {}",
                    file.getOriginalFilename(), expectedExtension, e);
            return false;
        }
    }

    /**
     * 파일에서 헤더 바이트를 읽어옴
     * 
     * @param file       읽을 파일
     * @param headerSize 읽을 헤더 크기
     * @return 파일 헤더 바이트 배열
     */
    private static byte[] readFileHeader(MultipartFile file, int headerSize) {
        try {
            byte[] header = new byte[headerSize];
            int bytesRead = file.getInputStream().read(header);

            if (bytesRead == -1) {
                return new byte[0];
            }

            // 실제 읽은 바이트만큼만 반환
            return Arrays.copyOf(header, bytesRead);

        } catch (IOException e) {
            log.error("파일 헤더 읽기 실패 - fileName: {}", file.getOriginalFilename(), e);
            return null;
        }
    }

    /**
     * 파일 헤더가 예상 시그니처와 일치하는지 확인
     * 
     * @param fileHeader 파일 헤더 바이트
     * @param signature  예상 시그니처 바이트
     * @return 일치하면 true
     */
    private static boolean matchesSignature(byte[] fileHeader, byte[] signature) {
        if (fileHeader.length < signature.length) {
            return false;
        }

        // WebP 특별 처리: RIFF 헤더 + WEBP 식별자 검증
        if (signature.length == 4 &&
                signature[0] == 'R' && signature[1] == 'I' &&
                signature[2] == 'F' && signature[3] == 'F') {
            return isWebPFile(fileHeader);
        }

        // 일반적인 시그니처 매칭
        for (int i = 0; i < signature.length; i++) {
            if (fileHeader[i] != signature[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * WebP 파일 특별 검증
     * WebP 구조: RIFF(4바이트) + 파일크기(4바이트) + WEBP(4바이트)
     * 
     * @param fileHeader 파일 헤더 바이트 배열
     * @return WebP 파일이면 true
     */
    private static boolean isWebPFile(byte[] fileHeader) {
        // 최소 12바이트 필요 (RIFF + 파일크기 + WEBP)
        if (fileHeader.length < 12) {
            return false;
        }

        // RIFF 헤더 확인 (0-3번째 바이트)
        if (fileHeader[0] != 'R' || fileHeader[1] != 'I' ||
                fileHeader[2] != 'F' || fileHeader[3] != 'F') {
            return false;
        }

        // WEBP 식별자 확인 (8-11번째 바이트)
        if (fileHeader[8] != 'W' || fileHeader[9] != 'E' ||
                fileHeader[10] != 'B' || fileHeader[11] != 'P') {
            return false;
        }

        return true;
    }

    /**
     * 바이트 배열을 16진수 문자열로 변환 (디버깅용)
     * 
     * @param bytes 바이트 배열
     * @return 16진수 문자열
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    /**
     * 특정 확장자에 대한 시그니처가 정의되어 있는지 확인
     * 
     * @param extension 확장자
     * @return 시그니처가 정의되어 있으면 true
     */
    public static boolean hasSignatureForExtension(String extension) {
        return FILE_SIGNATURES.containsKey(extension.toLowerCase());
    }

    /**
     * 지원하는 모든 확장자 목록 반환
     * 
     * @return 지원하는 확장자 Set
     */
    public static java.util.Set<String> getSupportedExtensions() {
        return FILE_SIGNATURES.keySet();
    }
}
