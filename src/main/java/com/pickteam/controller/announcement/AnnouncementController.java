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

    /**
     * 공지사항 목록 조회 (보안 검증 강화)
     *
     * @param workspaceId 워크스페이스 ID
     * @param teamId 팀 ID (선택사항 - 팀별 필터링)
     * @return 공지사항 목록 (최신순 정렬)
     */
    @GetMapping
    public ResponseEntity<?> getAnnouncements(
            @PathVariable Long workspaceId,
            @RequestParam(required = false) Long teamId) {

        log.info("공지사항 목록 조회 요청 - 워크스페이스: {}, 팀: {}", workspaceId, teamId);

        try {
            List<AnnouncementResponse> announcements;
            String message;

            if (teamId != null) {
                // 팀별 공지사항 조회 (워크스페이스 보안 검증 포함)
                announcements = announcementService.getAnnouncementsByTeam(workspaceId, teamId);
                message = String.format("팀의 공지사항 %d개를 조회했습니다.", announcements.size());
            } else {
                // 워크스페이스 전체 공지사항 조회 (존재 여부 검증 포함)
                announcements = announcementService.getAnnouncementsByWorkspace(workspaceId);
                message = String.format("워크스페이스의 공지사항 %d개를 조회했습니다.", announcements.size());
            }

            log.info("공지사항 목록 조회 완료 - 결과 개수: {}", announcements.size());

            return ResponseEntity.ok(createSuccessResponse(message, announcements));

        } catch (EntityNotFoundException e) {
            log.warn("공지사항 목록 조회 실패 - 엔티티 없음: {}", e.getMessage());
            return createErrorResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("공지사항 목록 조회 실패 - 잘못된 요청: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", e.getMessage());
        } catch (Exception e) {
            log.error("공지사항 목록 조회 중 예상치 못한 오류 발생", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                    "서버 내부 오류가 발생했습니다.");
        }
    }

    /**
     * 단일 공지사항 상세 조회 (워크스페이스 보안 검증 포함)
     *
     * @param workspaceId 워크스페이스 ID
     * @param announcementId 공지사항 ID
     * @return 공지사항 상세 정보
     */
    @GetMapping("/{announcementId}")
    public ResponseEntity<?> getAnnouncement(
            @PathVariable Long workspaceId,
            @PathVariable Long announcementId) {

        log.info("단일 공지사항 조회 요청 - 워크스페이스: {}, 공지사항: {}", workspaceId, announcementId);

        try {
            // 워크스페이스 보안 검증 포함한 공지사항 조회
            AnnouncementResponse response = announcementService.getAnnouncement(workspaceId, announcementId);

            log.info("단일 공지사항 조회 완료 - 제목: {}", response.getTitle());

            return ResponseEntity.ok(createSuccessResponse("공지사항을 조회했습니다.", response));

        } catch (EntityNotFoundException e) {
            log.warn("단일 공지사항 조회 실패 - 공지사항 없음: {}", e.getMessage());
            return createErrorResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("단일 공지사항 조회 실패 - 잘못된 요청: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", e.getMessage());
        } catch (Exception e) {
            log.error("단일 공지사항 조회 중 예상치 못한 오류 발생", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                    "서버 내부 오류가 발생했습니다.");
        }
    }

    /**
     * 공지사항 수정
     *
     * @param workspaceId 워크스페이스 ID
     * @param announcementId 공지사항 ID
     * @param request 공지사항 수정 요청 DTO
     * @param accountId 수정 요청자 계정 ID
     * @return 수정된 공지사항 정보
     *
     * API: PATCH /api/workspaces/{workspaceId}/announcement/{announcementId}
     */
    @PatchMapping("/{announcementId}")
    public ResponseEntity<?> updateAnnouncement(
            @PathVariable Long workspaceId,
            @PathVariable Long announcementId,
            @Valid @RequestBody AnnouncementUpdateRequest request,
            @RequestHeader(value = "Account-Id") Long accountId) {

        log.info("공지사항 수정 요청 - 워크스페이스: {}, 공지사항: {}, 계정: {}",
                workspaceId, announcementId, accountId);

        try {
            // 워크스페이스 보안 검증 포함한 공지사항 수정
            AnnouncementResponse response = announcementService
                    .updateAnnouncement(workspaceId, announcementId, request, accountId);

            log.info("공지사항 수정 완료 - 제목: {}", response.getTitle());

            return ResponseEntity.ok(createSuccessResponse("공지사항이 성공적으로 수정되었습니다.", response));

        } catch (EntityNotFoundException e) {
            log.warn("공지사항 수정 실패 - 공지사항 없음: {}", e.getMessage());
            return createErrorResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("공지사항 수정 실패 - 권한 없음 또는 잘못된 요청: {}", e.getMessage());
            return createErrorResponse(HttpStatus.FORBIDDEN, "FORBIDDEN", e.getMessage());
        } catch (Exception e) {
            log.error("공지사항 수정 중 예상치 못한 오류 발생", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                    "서버 내부 오류가 발생했습니다.");
        }
    }

    /**
     * 공지사항 삭제 (소프트 삭제)
     *
     * @param workspaceId 워크스페이스 ID
     * @param announcementId 공지사항 ID
     * @param accountId 삭제 요청자 계정 ID
     * @return 삭제 결과
     *
     * API: DELETE /api/workspaces/{workspaceId}/announcement/{announcementId}
     */
    @DeleteMapping("/{announcementId}")
    public ResponseEntity<?> deleteAnnouncement(
            @PathVariable Long workspaceId,
            @PathVariable Long announcementId,
            @RequestHeader(value = "Account-Id") Long accountId) {

        log.info("공지사항 삭제 요청 - 워크스페이스: {}, 공지사항: {}, 계정: {}",
                workspaceId, announcementId, accountId);

        try {
            // 워크스페이스 보안 검증 포함한 공지사항 삭제
            announcementService.deleteAnnouncement(workspaceId, announcementId, accountId);

            log.info("공지사항 삭제 완료 - 공지사항 ID: {}", announcementId);

            return ResponseEntity.ok(createSuccessResponse("공지사항이 성공적으로 삭제되었습니다.", null));

        } catch (EntityNotFoundException e) {
            log.warn("공지사항 삭제 실패 - 공지사항 없음: {}", e.getMessage());
            return createErrorResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("공지사항 삭제 실패 - 권한 없음 또는 잘못된 요청: {}", e.getMessage());
            return createErrorResponse(HttpStatus.FORBIDDEN, "FORBIDDEN", e.getMessage());
        } catch (Exception e) {
            log.error("공지사항 삭제 중 예상치 못한 오류 발생", e);
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