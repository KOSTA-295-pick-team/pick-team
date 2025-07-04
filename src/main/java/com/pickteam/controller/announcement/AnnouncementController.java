package com.pickteam.controller.announcement;

import com.pickteam.dto.announcement.AnnouncementCreateRequest;
import com.pickteam.dto.announcement.AnnouncementResponse;
import com.pickteam.dto.announcement.AnnouncementUpdateRequest;
import com.pickteam.service.announcement.AnnouncementService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 공지사항 컨트롤러
 * 워크스페이스 및 팀 내 공지사항 관리 API 제공
 */
@RestController
@RequestMapping("/api/workspaces/{workspaceId}/announcement")
@RequiredArgsConstructor
@Slf4j
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * 공지사항 등록
     * POST /api/workspaces/{workspaceId}/announcement
     */
    @PostMapping
    public ResponseEntity<?> createAnnouncement(
            @PathVariable Long workspaceId,
            @Valid @RequestBody AnnouncementCreateRequest request,
            @RequestHeader(value = "Account-Id") Long accountId) {
        
        log.info("공지사항 생성 API 호출 - 워크스페이스 ID: {}, 계정 ID: {}", workspaceId, accountId);
        
        try {
            AnnouncementResponse response = announcementService.createAnnouncement(request, accountId);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(createSuccessResponse("공지사항이 성공적으로 생성되었습니다.", response));
            
        } catch (EntityNotFoundException e) {
            return createErrorResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", e.getMessage());
        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", e.getMessage());
        } catch (Exception e) {
            log.error("공지사항 생성 중 예상치 못한 오류 발생", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", 
                    "서버 내부 오류가 발생했습니다.");
        }
    }


    // === 응답 생성 헬퍼 메서드들 ===

    /**
     * 성공 응답 생성
     */
    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now());
        if (data != null) {
            response.put("data", data);
        }
        return response;
    }

    /**
     * 오류 응답 생성
     */
    private ResponseEntity<?> createErrorResponse(HttpStatus status, String errorCode, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", errorCode);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(status).body(errorResponse);
    }
}