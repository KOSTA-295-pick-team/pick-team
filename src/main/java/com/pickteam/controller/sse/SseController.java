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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Optional;

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
        Long accountId = account.getId();
        sseSessionRepository.savePrepared(accountId);
        log.info("🔐 SSE 등록 완료: accountId={}", accountId);
        return ResponseEntity.ok().build();
    }

    /**
     * Step 2: 실제 SSE 연결 (토큰 없이 호출됨)
     */
    @GetMapping("/subscribe")
    public SseEmitter subscribe() {
        log.debug("🔍 SSE subscribe 요청 시작");
        
        Optional<Long> accountIdOpt = sseSessionRepository.resolveAnyPrepared();
        if (accountIdOpt.isEmpty()) {
            log.warn("❌ 등록된 SSE 사용자가 없습니다. 현재 준비된 세션 수: {}", 
                    sseSessionRepository.countPreparedSessions());
            throw new IllegalStateException("❌ 등록된 SSE 사용자가 없습니다.");
        }
        
        Long accountId = accountIdOpt.get();
        log.info("🔗 SSE 연결 시작: accountId={}", accountId);
        
        return sseService.connect(accountId);
    }
}