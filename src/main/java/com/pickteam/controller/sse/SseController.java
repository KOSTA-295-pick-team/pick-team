package com.pickteam.controller.sse;

import com.pickteam.domain.user.Account;
import com.pickteam.security.UserPrincipal;
import com.pickteam.service.sse.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SseController {

    private final SseService sseService;

    @GetMapping("/subscribe")
    public SseEmitter subscribe(@AuthenticationPrincipal UserPrincipal account) {
        Long accountId = account.getId(); // Account에서 계정 ID 추출
        return sseService.connect(accountId);
    }
}