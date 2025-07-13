package com.pickteam.service.sse;

import com.pickteam.repository.sse.SseSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {
    // 실시간 연결된 사용자 세션 (accountId → emitter)
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * SSE 연결
     */
    public SseEmitter connect(Long accountId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        emitter.onCompletion(() -> {
            emitters.remove(accountId);
            log.info("🛑 SSE 연결 종료됨: {}", accountId);
        });

        emitter.onTimeout(() -> {
            emitters.remove(accountId);
            log.info("⏰ SSE 타임아웃: {}", accountId);
        });

        emitters.put(accountId, emitter);
        log.info("✅ SSE 연결 완료: {}", accountId);

        return emitter;
    }

    /**
     * 기본 메시지 전송 (eventName: "alert")
     */
    public void sendToUser(Long accountId, String message) {
        log.debug("이벤트 전송 시작: accountId={}", accountId);
        sendToUser(accountId, "alert", message);
    }

    /**
     * 커스텀 이벤트 + DTO 객체 전송
     */
    public void sendToUser(Long accountId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(accountId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
                log.info("📡 SSE 전송 완료: {} → {} / {}", accountId, eventName, data);
            } catch (IOException e) {
                emitters.remove(accountId);
                log.warn("⚠️ SSE 전송 실패 (emitter 제거): {}", accountId, e);
            }
        } else {
            log.info("ℹ️ SSE 미연결 사용자: {}", accountId);
        }
    }

    /**
     * 수동 연결 해제
     */
    public void disconnect(Long accountId) {
        SseEmitter emitter = emitters.remove(accountId);
        if (emitter != null) {
            emitter.complete();
            log.info("🧹 수동 연결 해제: {}", accountId);
        }
    }
}
