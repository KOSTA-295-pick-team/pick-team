package com.pickteam.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.UUID;

/**
 * OAuth State 관리 서비스
 * - CSRF 공격 방지를 위한 state 값 생성, 저장, 검증
 * - HttpSession을 사용한 state 관리
 */
@Slf4j
@Service
public class OAuthStateService {

    private static final String OAUTH_STATE_KEY = "oauth_state";

    /**
     * OAuth state 값 생성 및 세션에 저장
     * 
     * @return 생성된 state 값
     */
    public String generateAndStoreState() {
        String state = UUID.randomUUID().toString();

        try {
            HttpSession session = getCurrentSession();
            session.setAttribute(OAUTH_STATE_KEY, state);
            log.debug("OAuth state 저장 완료: {}", state.substring(0, 8) + "***");
        } catch (Exception e) {
            log.warn("OAuth state 저장 실패, 세션 없음", e);
            throw new RuntimeException("세션을 사용할 수 없습니다");
        }

        return state;
    }

    /**
     * OAuth state 값 검증
     * 
     * @param receivedState 콜백에서 받은 state 값
     * @return 검증 성공 여부
     */
    public boolean validateState(String receivedState) {
        if (receivedState == null || receivedState.trim().isEmpty()) {
            log.warn("OAuth state 검증 실패: 받은 state가 없음");
            return false;
        }

        try {
            HttpSession session = getCurrentSession();
            String storedState = (String) session.getAttribute(OAUTH_STATE_KEY);

            if (storedState == null) {
                log.warn("OAuth state 검증 실패: 저장된 state가 없음");
                return false;
            }

            boolean isValid = storedState.equals(receivedState);

            if (isValid) {
                // 검증 성공 시 세션에서 state 제거 (일회성)
                session.removeAttribute(OAUTH_STATE_KEY);
                log.debug("OAuth state 검증 성공");
            } else {
                log.warn("OAuth state 검증 실패: state 불일치");
            }

            return isValid;

        } catch (Exception e) {
            log.error("OAuth state 검증 중 오류 발생", e);
            return false;
        }
    }

    /**
     * 현재 HTTP 세션 가져오기
     * 
     * @return 현재 HttpSession
     * @throws RuntimeException 세션을 가져올 수 없는 경우
     */
    private HttpSession getCurrentSession() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attrs.getRequest().getSession(true);
    }

    /**
     * 저장된 state 정리 (타임아웃 등의 경우)
     */
    public void clearStoredState() {
        try {
            HttpSession session = getCurrentSession();
            session.removeAttribute(OAUTH_STATE_KEY);
            log.debug("OAuth state 정리 완료");
        } catch (Exception e) {
            log.debug("OAuth state 정리 중 오류 (무시): {}", e.getMessage());
        }
    }
}
