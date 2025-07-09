package com.pickteam.service.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pickteam.dto.security.JwtAuthenticationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * OAuth 임시 코드 관리 서비스
 * - JWT 토큰을 URL에 직접 노출하지 않고 임시 코드로 교환
 * - Redis를 사용한 임시 저장 (TTL: 5분)
 * - 보안성 향상을 위한 일회성 코드 사용
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthTempCodeService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String TEMP_CODE_PREFIX = "oauth_temp_code:";
    private static final String USED_CODE_PREFIX = "oauth_used_code:";
    private static final Duration TEMP_CODE_TTL = Duration.ofMinutes(5); // 5분 유효
    private static final Duration USED_CODE_TTL = Duration.ofSeconds(30); // 사용된 코드 30초 캐시

    /**
     * 임시 코드 생성 및 JWT 토큰 저장
     * 
     * @param jwtResponse JWT 토큰 응답
     * @return 생성된 임시 코드
     */
    public String generateTempCodeAndStoreTokens(JwtAuthenticationResponse jwtResponse) {
        String tempCode = generateTempCode();
        String redisKey = TEMP_CODE_PREFIX + tempCode;

        try {
            // Redis에 JWT 토큰 정보 저장 (5분 TTL)
            redisTemplate.opsForValue().set(redisKey, jwtResponse, TEMP_CODE_TTL);

            log.debug("OAuth 임시 코드 생성 및 저장 완료: {}", tempCode.substring(0, 8) + "***");
            return tempCode;

        } catch (Exception e) {
            log.error("OAuth 임시 코드 저장 실패", e);
            throw new RuntimeException("임시 코드 생성에 실패했습니다", e);
        }
    }

    /**
     * 임시 코드로 JWT 토큰 조회 및 중복 요청 처리
     * 
     * @param tempCode 임시 코드
     * @return JWT 토큰 응답 (없으면 null)
     */
    public JwtAuthenticationResponse exchangeTokensWithTempCode(String tempCode) {
        if (tempCode == null || tempCode.trim().isEmpty()) {
            log.warn("OAuth 임시 코드 교환 실패: 코드가 없음");
            return null;
        }

        String redisKey = TEMP_CODE_PREFIX + tempCode;
        String usedKey = USED_CODE_PREFIX + tempCode;

        try {
            // 이미 사용된 코드인지 확인 (중복 요청 처리)
            Object usedToken = redisTemplate.opsForValue().get(usedKey);
            if (usedToken != null) {
                log.debug("OAuth 임시 코드 중복 요청 처리: {}", tempCode.substring(0, 8) + "***");

                if (usedToken instanceof JwtAuthenticationResponse) {
                    return (JwtAuthenticationResponse) usedToken;
                } else if (usedToken instanceof LinkedHashMap) {
                    try {
                        return objectMapper.convertValue(usedToken, JwtAuthenticationResponse.class);
                    } catch (Exception e) {
                        log.error("중복 요청 처리 시 LinkedHashMap 변환 실패", e);
                        redisTemplate.delete(usedKey); // 잘못된 데이터 삭제
                    }
                }
            }

            // Redis에서 JWT 토큰 조회
            Object storedValue = redisTemplate.opsForValue().get(redisKey);

            if (storedValue == null) {
                log.warn("OAuth 임시 코드 교환 실패: 유효하지 않거나 만료된 코드");
                return null;
            }

            // 디버깅을 위한 상세 로그
            log.debug("Redis에서 조회된 객체 정보 - 타입: {}, 클래스: {}, 값: {}",
                    storedValue.getClass().getSimpleName(),
                    storedValue.getClass().getName(),
                    storedValue.toString().length() > 200 ? storedValue.toString().substring(0, 200) + "..."
                            : storedValue.toString());

            JwtAuthenticationResponse jwtResponse;

            if (storedValue instanceof JwtAuthenticationResponse) {
                jwtResponse = (JwtAuthenticationResponse) storedValue;
            } else if (storedValue instanceof LinkedHashMap) {
                // LinkedHashMap을 JwtAuthenticationResponse로 변환
                log.debug("LinkedHashMap을 JwtAuthenticationResponse로 변환 시작");
                try {
                    jwtResponse = objectMapper.convertValue(storedValue, JwtAuthenticationResponse.class);
                    log.debug("LinkedHashMap 변환 성공");
                } catch (Exception e) {
                    log.error("LinkedHashMap을 JwtAuthenticationResponse로 변환 실패", e);
                    return null;
                }
            } else {
                log.error("OAuth 임시 코드 교환 실패: 지원하지 않는 데이터 타입 - 예상: JwtAuthenticationResponse 또는 LinkedHashMap, 실제: {}",
                        storedValue.getClass().getName());
                return null;
            }

            // 원본 코드 삭제
            redisTemplate.delete(redisKey);

            // 중복 요청 방지를 위해 짧은 시간 동안 결과 캐시
            redisTemplate.opsForValue().set(usedKey, jwtResponse, USED_CODE_TTL);

            // 응답 내용 검증 로그
            log.debug("OAuth 임시 코드 교환 성공: {} - accessToken: {}, user: {}, userType: {}",
                    tempCode.substring(0, 8) + "***",
                    jwtResponse.getAccessToken() != null ? "존재" : "null",
                    jwtResponse.getUser() != null ? jwtResponse.getUser().toString() : "null",
                    jwtResponse.getUser() != null ? jwtResponse.getUser().getClass().getName() : "null");

            return jwtResponse;

        } catch (Exception e) {
            log.error("OAuth 임시 코드 교환 중 오류 발생", e);
            return null;
        }
    }

    /**
     * 임시 코드 유효성 검사
     * 
     * @param tempCode 임시 코드
     * @return 유효 여부
     */
    public boolean isValidTempCode(String tempCode) {
        if (tempCode == null || tempCode.trim().isEmpty()) {
            return false;
        }

        String redisKey = TEMP_CODE_PREFIX + tempCode;

        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
        } catch (Exception e) {
            log.error("OAuth 임시 코드 유효성 검사 중 오류 발생", e);
            return false;
        }
    }

    /**
     * 만료된 임시 코드 정리 (Redis TTL로 자동 처리되지만 수동 정리용)
     * 
     * @param tempCode 임시 코드
     */
    public void clearTempCode(String tempCode) {
        if (tempCode == null || tempCode.trim().isEmpty()) {
            return;
        }

        String redisKey = TEMP_CODE_PREFIX + tempCode;

        try {
            redisTemplate.delete(redisKey);
            log.debug("OAuth 임시 코드 정리 완료: {}", tempCode.substring(0, 8) + "***");
        } catch (Exception e) {
            log.debug("OAuth 임시 코드 정리 중 오류 (무시): {}", e.getMessage());
        }
    }

    /**
     * 임시 코드 생성
     * 
     * @return UUID 기반 임시 코드
     */
    private String generateTempCode() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
