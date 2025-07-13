package com.pickteam.controller.sse;

import com.pickteam.domain.user.Account;
import com.pickteam.repository.sse.SseSessionRepository;
import com.pickteam.security.UserPrincipal;
import com.pickteam.service.sse.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

import java.util.Map;
nimport java.util.Map;

@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
@Slf4j
public class SseController {

    private final SseService sseService;
    private final SseSessionRepository sseSessionRepository;

    /**
     * Step 1: 사용자 인증 후 SSE 연결 준비 등록
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(@AuthenticationPrincipal UserPrincipal account) {

    /**
     * Step 1-Token: 사용자 인증 후 토큰 기반 SSE 연결 준비 등록
     */
    @PostMapping("/register-token")
    public ResponseEntity<Map<String, String>> registerWithToken(@AuthenticationPrincipal UserPrincipal account) {
        Long accountId = account.getId();
        String token = sseSessionRepository.savePreparedWithToken(accountId);
        log.info("🔐 SSE 토큰 등록 완료: accountId={}, token={}", accountId, token);
        return ResponseEntity.ok(Map.of("token", token));
    }
        Long accountId = account.getId();

    /**
     * Step 1-Token: 사용자 인증 후 토큰 기반 SSE 연결 준비 등록
     */
    @PostMapping("/register-token")
    public ResponseEntity<Map<String, String>> registerWithToken(@AuthenticationPrincipal UserPrincipal account) {
        Long accountId = account.getId();
        String token = sseSessionRepository.savePreparedWithToken(accountId);
        log.info("🔐 SSE 토큰 등록 완료: accountId={}, token={}", accountId, token);
        return ResponseEntity.ok(Map.of("token", token));
    }
        sseSessionRepository.savePrepared(accountId);

    /**
     * Step 1-Token: 사용자 인증 후 토큰 기반 SSE 연결 준비 등록
     */
    @PostMapping("/register-token")
    public ResponseEntity<Map<String, String>> registerWithToken(@AuthenticationPrincipal UserPrincipal account) {
        Long accountId = account.getId();
        String token = sseSessionRepository.savePreparedWithToken(accountId);
        log.info("🔐 SSE 토큰 등록 완료: accountId={}, token={}", accountId, token);
        return ResponseEntity.ok(Map.of("token", token));
    }
        log.info("🔐 SSE 등록 완료: accountId={}", accountId);

    /**
     * Step 1-Token: 사용자 인증 후 토큰 기반 SSE 연결 준비 등록
     */
    @PostMapping("/register-token")
    public ResponseEntity<Map<String, String>> registerWithToken(@AuthenticationPrincipal UserPrincipal account) {
        Long accountId = account.getId();
        String token = sseSessionRepository.savePreparedWithToken(accountId);
        log.info("🔐 SSE 토큰 등록 완료: accountId={}, token={}", accountId, token);
        return ResponseEntity.ok(Map.of("token", token));
    }
        return ResponseEntity.ok().build();

    /**
     * Step 1-Token: 사용자 인증 후 토큰 기반 SSE 연결 준비 등록
     */
    @PostMapping("/register-token")
    public ResponseEntity<Map<String, String>> registerWithToken(@AuthenticationPrincipal UserPrincipal account) {
        Long accountId = account.getId();
        String token = sseSessionRepository.savePreparedWithToken(accountId);
        log.info("🔐 SSE 토큰 등록 완료: accountId={}, token={}", accountId, token);
        return ResponseEntity.ok(Map.of("token", token));
    }

    /**
     * Step 1-Token: 사용자 인증 후 토큰 기반 SSE 연결 준비 등록
     */
    @PostMapping("/register-token")
    public ResponseEntity<Map<String, String>> registerWithToken(@AuthenticationPrincipal UserPrincipal account) {
        Long accountId = account.getId();
        String token = sseSessionRepository.savePreparedWithToken(accountId);
        log.info("🔐 SSE 토큰 등록 완료: accountId={}, token={}", accountId, token);
        return ResponseEntity.ok(Map.of("token", token));
    }
    }

    /**
     * Step 2: 실제 SSE 연결 (토큰 없이 호출됨)
     */
    @GetMapping("/subscribe")
    public SseEmitter subscribe() {

    /**
     * Step 2-Token: 토큰 기반 실제 SSE 연결
     */
    @GetMapping("/subscribe-token")
    public SseEmitter subscribeWithToken(@RequestParam String token) {
        Long accountId = sseSessionRepository.resolvePreparedByToken(token)
                .orElseThrow(() -> new IllegalStateException("❌ 유효하지 않은 토큰입니다."));
        log.info("🔗 토큰 기반 SSE 연결: accountId={}", accountId);
        return sseService.connect(accountId);
    }
        Long accountId = sseSessionRepository.resolveAnyPrepared()

    /**
     * Step 2-Token: 토큰 기반 실제 SSE 연결
     */
    @GetMapping("/subscribe-token")
    public SseEmitter subscribeWithToken(@RequestParam String token) {
        Long accountId = sseSessionRepository.resolvePreparedByToken(token)
                .orElseThrow(() -> new IllegalStateException("❌ 유효하지 않은 토큰입니다."));
        log.info("🔗 토큰 기반 SSE 연결: accountId={}", accountId);
        return sseService.connect(accountId);
    }
                .orElseThrow(() -> new IllegalStateException("❌ 등록된 SSE 사용자가 없습니다."));

    /**
     * Step 2-Token: 토큰 기반 실제 SSE 연결
     */
    @GetMapping("/subscribe-token")
    public SseEmitter subscribeWithToken(@RequestParam String token) {
        Long accountId = sseSessionRepository.resolvePreparedByToken(token)
                .orElseThrow(() -> new IllegalStateException("❌ 유효하지 않은 토큰입니다."));
        log.info("🔗 토큰 기반 SSE 연결: accountId={}", accountId);
        return sseService.connect(accountId);
    }
        return sseService.connect(accountId);

    /**
     * Step 2-Token: 토큰 기반 실제 SSE 연결
     */
    @GetMapping("/subscribe-token")
    public SseEmitter subscribeWithToken(@RequestParam String token) {
        Long accountId = sseSessionRepository.resolvePreparedByToken(token)
                .orElseThrow(() -> new IllegalStateException("❌ 유효하지 않은 토큰입니다."));
        log.info("🔗 토큰 기반 SSE 연결: accountId={}", accountId);
        return sseService.connect(accountId);
    }


    /**
     * Step 2-Token: 토큰 기반 실제 SSE 연결
     */
    @GetMapping("/subscribe-token")
    public SseEmitter subscribeWithToken(@RequestParam String token) {
        Long accountId = sseSessionRepository.resolvePreparedByToken(token)
                .orElseThrow(() -> new IllegalStateException("❌ 유효하지 않은 토큰입니다."));
        log.info("🔗 토큰 기반 SSE 연결: accountId={}", accountId);
        return sseService.connect(accountId);
    }
    /**

    /**
     * Step 2-Token: 토큰 기반 실제 SSE 연결
     */
    @GetMapping("/subscribe-token")
    public SseEmitter subscribeWithToken(@RequestParam String token) {
        Long accountId = sseSessionRepository.resolvePreparedByToken(token)
                .orElseThrow(() -> new IllegalStateException("❌ 유효하지 않은 토큰입니다."));
        log.info("🔗 토큰 기반 SSE 연결: accountId={}", accountId);
        return sseService.connect(accountId);
    }
     * Step 2-Token: 토큰 기반 실제 SSE 연결

    /**
     * Step 2-Token: 토큰 기반 실제 SSE 연결
     */
    @GetMapping("/subscribe-token")
    public SseEmitter subscribeWithToken(@RequestParam String token) {
        Long accountId = sseSessionRepository.resolvePreparedByToken(token)
                .orElseThrow(() -> new IllegalStateException("❌ 유효하지 않은 토큰입니다."));
        log.info("🔗 토큰 기반 SSE 연결: accountId={}", accountId);
        return sseService.connect(accountId);
    }
     */

    /**
     * Step 2-Token: 토큰 기반 실제 SSE 연결
     */
    @GetMapping("/subscribe-token")
    public SseEmitter subscribeWithToken(@RequestParam String token) {
        Long accountId = sseSessionRepository.resolvePreparedByToken(token)
                .orElseThrow(() -> new IllegalStateException("❌ 유효하지 않은 토큰입니다."));
        log.info("🔗 토큰 기반 SSE 연결: accountId={}", accountId);
        return sseService.connect(accountId);
    }
    @GetMapping("/subscribe-token")

    /**
     * Step 2-Token: 토큰 기반 실제 SSE 연결
     */
    @GetMapping("/subscribe-token")
    public SseEmitter subscribeWithToken(@RequestParam String token) {
        Long accountId = sseSessionRepository.resolvePreparedByToken(token)
                .orElseThrow(() -> new IllegalStateException("❌ 유효하지 않은 토큰입니다."));
        log.info("🔗 토큰 기반 SSE 연결: accountId={}", accountId);
        return sseService.connect(accountId);
    }
    public SseEmitter subscribeWithToken(@RequestParam String token) {

    /**
     * Step 2-Token: 토큰 기반 실제 SSE 연결
     */
    @GetMapping("/subscribe-token")
    public SseEmitter subscribeWithToken(@RequestParam String token) {
        Long accountId = sseSessionRepository.resolvePreparedByToken(token)
                .orElseThrow(() -> new IllegalStateException("❌ 유효하지 않은 토큰입니다."));
        log.info("🔗 토큰 기반 SSE 연결: accountId={}", accountId);
        return sseService.connect(accountId);
    }
        Long accountId = sseSessionRepository.resolvePreparedByToken(token)

    /**
     * Step 2-Token: 토큰 기반 실제 SSE 연결
     */
    @GetMapping("/subscribe-token")
    public SseEmitter subscribeWithToken(@RequestParam String token) {
        Long accountId = sseSessionRepository.resolvePreparedByToken(token)
                .orElseThrow(() -> new IllegalStateException("❌ 유효하지 않은 토큰입니다."));
        log.info("🔗 토큰 기반 SSE 연결: accountId={}", accountId);
        return sseService.connect(accountId);
    }
                .orElseThrow(() -> new IllegalStateException("❌ 유효하지 않은 토큰입니다."));

    /**
     * Step 2-Token: 토큰 기반 실제 SSE 연결
     */
    @GetMapping("/subscribe-token")
    public SseEmitter subscribeWithToken(@RequestParam String token) {
        Long accountId = sseSessionRepository.resolvePreparedByToken(token)
                .orElseThrow(() -> new IllegalStateException("❌ 유효하지 않은 토큰입니다."));
        log.info("🔗 토큰 기반 SSE 연결: accountId={}", accountId);
        return sseService.connect(accountId);
    }
        log.info("🔗 토큰 기반 SSE 연결: accountId={}", accountId);

    /**
     * Step 2-Token: 토큰 기반 실제 SSE 연결
     */
    @GetMapping("/subscribe-token")
    public SseEmitter subscribeWithToken(@RequestParam String token) {
        Long accountId = sseSessionRepository.resolvePreparedByToken(token)
                .orElseThrow(() -> new IllegalStateException("❌ 유효하지 않은 토큰입니다."));
        log.info("🔗 토큰 기반 SSE 연결: accountId={}", accountId);
        return sseService.connect(accountId);
    }
        return sseService.connect(accountId);
    }

    /**
     * Step 2-Token: 토큰 기반 실제 SSE 연결
     */
    @GetMapping("/subscribe-token")
    public SseEmitter subscribeWithToken(@RequestParam String token) {
        Long accountId = sseSessionRepository.resolvePreparedByToken(token)
                .orElseThrow(() -> new IllegalStateException("❌ 유효하지 않은 토큰입니다."));
        log.info("🔗 토큰 기반 SSE 연결: accountId={}", accountId);
        return sseService.connect(accountId);

    /**
     * Step 2-Token: 토큰 기반 실제 SSE 연결
     */
    @GetMapping("/subscribe-token")
    public SseEmitter subscribeWithToken(@RequestParam String token) {
        Long accountId = sseSessionRepository.resolvePreparedByToken(token)
                .orElseThrow(() -> new IllegalStateException("❌ 유효하지 않은 토큰입니다."));
        log.info("🔗 토큰 기반 SSE 연결: accountId={}", accountId);
        return sseService.connect(accountId);
    }
    }
    }
}