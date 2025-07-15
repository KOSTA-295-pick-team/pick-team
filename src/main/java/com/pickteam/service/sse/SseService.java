package com.pickteam.service.sse;

import com.pickteam.repository.sse.SseSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {
    // 다중 연결 지원: sessionId → emitter
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    // 사용자별 세션 관리: accountId → Set<sessionId>
    private final Map<Long, Set<String>> userSessions = new ConcurrentHashMap<>();

    /**
     * SSE 연결 (다중 연결 지원)
     */
    public SseEmitter connect(Long accountId) {
        // 고유 세션 ID 생성
        String sessionId = UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L); // 30분

        emitter.onCompletion(() -> {
            removeSession(accountId, sessionId);
            log.info("🛑 SSE 연결 종료됨: accountId={}, sessionId={}", accountId, sessionId);
        });

        emitter.onTimeout(() -> {
            removeSession(accountId, sessionId);
            log.info("⏰ SSE 타임아웃: accountId={}, sessionId={}", accountId, sessionId);
        });

        emitter.onError((throwable) -> {
            removeSession(accountId, sessionId);
            log.warn("❌ SSE 연결 오류: accountId={}, sessionId={}, error={}", 
                    accountId, sessionId, throwable.getMessage());
        });

        // 세션 등록
        emitters.put(sessionId, emitter);
        userSessions.computeIfAbsent(accountId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        
        log.info("✅ SSE 연결 완료: accountId={}, sessionId={}, 총 세션 수: {}", 
                accountId, sessionId, userSessions.get(accountId).size());

        return emitter;
    }

    /**
     * 세션 정리 헬퍼 메서드
     */
    private void removeSession(Long accountId, String sessionId) {
        emitters.remove(sessionId);
        Set<String> sessions = userSessions.get(accountId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                userSessions.remove(accountId);
            }
        }
    }

    /**
     * 기본 메시지 전송 (eventName: "alert") - 모든 세션에 전송
     */
    public void sendToUser(Long accountId, String message) {
        log.debug("이벤트 전송 시작: accountId={}", accountId);
        sendToUser(accountId, "alert", message);
    }

    /**
     * 커스텀 이벤트 + DTO 객체 전송 - 모든 세션에 전송
     */
    public void sendToUser(Long accountId, String eventName, Object data) {
        Set<String> sessions = userSessions.get(accountId);
        if (sessions == null || sessions.isEmpty()) {
            log.info("ℹ️ SSE 미연결 사용자: {}", accountId);
            return;
        }

        int successCount = 0;
        int failCount = 0;
        
        for (String sessionId : Set.copyOf(sessions)) { // 동시성 문제 방지
            SseEmitter emitter = emitters.get(sessionId);
            if (emitter != null) {
                try {
                    emitter.send(SseEmitter.event()
                            .name(eventName)
                            .data(data));
                    successCount++;
                } catch (IOException e) {
                    removeSession(accountId, sessionId);
                    failCount++;
                    log.warn("⚠️ SSE 전송 실패 (세션 제거): accountId={}, sessionId={}", 
                            accountId, sessionId, e);
                }
            }
        }
        
        log.info("📡 SSE 전송 완료: accountId={}, 이벤트={}, 성공={}, 실패={}, 데이터={}", 
                accountId, eventName, successCount, failCount, data);
    }

    /**
     * 여러 사용자에게 동시 메시지 전송 (그룹 브로드캐스트)
     */
    public void sendToUsers(Set<Long> accountIds, String eventName, Object data) {
        if (accountIds == null || accountIds.isEmpty()) {
            log.warn("⚠️ 브로드캐스트 대상 사용자가 없습니다.");
            return;
        }

        int totalUsers = accountIds.size();
        int connectedUsers = 0;
        int totalSessions = 0;
        int successCount = 0;
        int failCount = 0;

        for (Long accountId : accountIds) {
            Set<String> sessions = userSessions.get(accountId);
            if (sessions != null && !sessions.isEmpty()) {
                connectedUsers++;
                totalSessions += sessions.size();
                
                // 각 사용자의 모든 세션에 전송
                for (String sessionId : Set.copyOf(sessions)) {
                    SseEmitter emitter = emitters.get(sessionId);
                    if (emitter != null) {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name(eventName)
                                    .data(data));
                            successCount++;
                        } catch (IOException e) {
                            removeSession(accountId, sessionId);
                            failCount++;
                            log.warn("⚠️ 그룹 전송 실패 (세션 제거): accountId={}, sessionId={}", 
                                    accountId, sessionId, e);
                        }
                    }
                }
            }
        }

        log.info("📡 그룹 브로드캐스트 완료: 대상={}명, 연결={}명, 총세션={}, 성공={}, 실패={}, 이벤트={}", 
                totalUsers, connectedUsers, totalSessions, successCount, failCount, eventName);
    }

    /**
     * 전체 연결된 사용자에게 브로드캐스트 (공지 등)
     */
    public void broadcastToAll(String eventName, Object data) {
        Set<Long> allUsers = new ConcurrentHashMap<>(userSessions).keySet();
        sendToUsers(allUsers, eventName, data);
    }

    /**
     * 특정 채팅방 멤버들에게 브로드캐스트
     */
    public void sendToChatRoom(Set<Long> chatMembers, String eventName, Object data) {
        log.info("🎯 채팅방 브로드캐스트 시작: 멤버={}명, 이벤트={}", chatMembers.size(), eventName);
        sendToUsers(chatMembers, eventName, data);
    }

    /**
     * 사용자의 모든 연결 해제
     */
    public void disconnect(Long accountId) {
        Set<String> sessions = userSessions.remove(accountId);
        if (sessions != null) {
            int disconnectedCount = 0;
            for (String sessionId : sessions) {
                SseEmitter emitter = emitters.remove(sessionId);
                if (emitter != null) {
                    emitter.complete();
                    disconnectedCount++;
                }
            }
            log.info("🧹 수동 연결 해제: accountId={}, 해제된 세션 수={}", accountId, disconnectedCount);
        }
    }

    /**
     * 연결 상태 조회 (디버깅용)
     */
    public Map<String, Object> getConnectionStatus() {
        Map<String, Object> status = new ConcurrentHashMap<>();
        status.put("totalEmitters", emitters.size());
        status.put("totalUsers", userSessions.size());
        
        Map<Long, Integer> userSessionCounts = new ConcurrentHashMap<>();
        userSessions.forEach((accountId, sessions) -> 
                userSessionCounts.put(accountId, sessions.size()));
        status.put("userSessionCounts", userSessionCounts);
        
        return status;
    }

    /**
     * 연결 통계 및 리소스 모니터링
     */
    public Map<String, Object> getDetailedConnectionStatus() {
        Map<String, Object> status = new ConcurrentHashMap<>();
        
        // 기본 통계
        status.put("totalEmitters", emitters.size());
        status.put("totalUsers", userSessions.size());
        
        // 사용자별 세션 수 분포
        Map<Long, Integer> userSessionCounts = new ConcurrentHashMap<>();
        int maxSessionsPerUser = 0;
        int totalActiveSessions = 0;
        
        for (Map.Entry<Long, Set<String>> entry : userSessions.entrySet()) {
            Long accountId = entry.getKey();
            int sessionCount = entry.getValue().size();
            userSessionCounts.put(accountId, sessionCount);
            maxSessionsPerUser = Math.max(maxSessionsPerUser, sessionCount);
            totalActiveSessions += sessionCount;
        }
        
        status.put("userSessionCounts", userSessionCounts);
        status.put("maxSessionsPerUser", maxSessionsPerUser);
        status.put("totalActiveSessions", totalActiveSessions);
        status.put("avgSessionsPerUser", userSessions.isEmpty() ? 0 : 
                (double) totalActiveSessions / userSessions.size());
        
        // 리소스 사용량 경고
        if (totalActiveSessions > 1000) {
            status.put("warning", "High session count detected: " + totalActiveSessions);
        }
        if (maxSessionsPerUser > 10) {
            status.put("warning", "User with excessive sessions detected: " + maxSessionsPerUser);
        }
        
        return status;
    }

    /**
     * 리소스 정리 (비활성 세션 제거)
     */
    public int cleanupInactiveSessions() {
        int cleanedCount = 0;
        
        for (Map.Entry<Long, Set<String>> entry : new ConcurrentHashMap<>(userSessions).entrySet()) {
            Long accountId = entry.getKey();
            Set<String> sessions = entry.getValue();
            
            for (String sessionId : Set.copyOf(sessions)) {
                SseEmitter emitter = emitters.get(sessionId);
                if (emitter == null) {
                    // emitter는 없는데 세션 맵에는 남아있는 경우
                    removeSession(accountId, sessionId);
                    cleanedCount++;
                    log.info("🧹 비활성 세션 정리: accountId={}, sessionId={}", accountId, sessionId);
                }
            }
        }
        
        log.info("✅ 세션 정리 완료: {}개 세션 정리됨", cleanedCount);
        return cleanedCount;
    }
}
