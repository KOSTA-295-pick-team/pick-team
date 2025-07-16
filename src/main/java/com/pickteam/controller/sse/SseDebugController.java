package com.pickteam.controller.sse;

import com.pickteam.service.sse.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
@Slf4j
public class SseDebugController {

    private final SseService sseService;

    /**
     * SSE 연결 상태 디버깅 API
     */
    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> getConnectionStatus() {
        Map<String, Object> status = sseService.getDetailedConnectionStatus();
        log.info("🔍 SSE 연결 상태 조회: {}", status);
        return ResponseEntity.ok(status);
    }

    /**
     * 비활성 세션 정리 API
     */
    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupSessions() {
        int cleanedCount = sseService.cleanupInactiveSessions();
        Map<String, Object> result = Map.of(
                "cleanedSessions", cleanedCount,
                "timestamp", System.currentTimeMillis()
        );
        log.info("🧹 세션 정리 요청 처리: {}", result);
        return ResponseEntity.ok(result);
    }

    /**
     * 테스트용 브로드캐스트 API
     */
    @PostMapping("/test-broadcast")
    public ResponseEntity<Map<String, Object>> testBroadcast() {
        sseService.broadcastToAll("SYSTEM_ANNOUNCEMENT", 
                Map.of("message", "시스템 테스트 메시지", "timestamp", System.currentTimeMillis()));
        
        Map<String, Object> result = Map.of(
                "status", "broadcast_sent",
                "timestamp", System.currentTimeMillis()
        );
        return ResponseEntity.ok(result);
    }
}
