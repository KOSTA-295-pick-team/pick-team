package com.pickteam.dto.user;

import com.pickteam.domain.enums.AuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OAuth 사용자 정보 공통 DTO
 * - 다양한 OAuth 제공자(구글, 카카오)로부터 받은 사용자 정보를 표준화
 * - 각 제공자별 응답 형식을 통일된 형식으로 변환
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthUserInfo {

    /** OAuth 제공자별 고유 사용자 ID */
    private String providerId;

    /** 사용자 이메일 주소 */
    private String email;

    /** 사용자 이름 */
    private String name;

    /** 프로필 이미지 URL */
    private String profileImageUrl;

    /** OAuth 제공자 (GOOGLE, KAKAO) */
    private AuthProvider provider;

    /** 이메일 인증 상태 (제공자에서 확인된 이메일인지) */
    @Builder.Default
    private boolean emailVerified = false;

    /**
     * 필수 정보가 모두 있는지 확인
     * 
     * @return 이메일과 providerId가 모두 있으면 true
     */
    public boolean isValid() {
        return providerId != null && !providerId.trim().isEmpty() &&
                email != null && !email.trim().isEmpty();
    }

    /**
     * 사용자 이름이 없을 때 기본 이름 생성
     * 
     * @return 이름이 있으면 그대로, 없으면 기본 이름 반환
     */
    public String getDisplayName() {
        if (name != null && !name.trim().isEmpty()) {
            return name.trim();
        }

        // 이메일에서 @앞부분을 이름으로 사용
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf("@"));
        }

        return "사용자" + providerId.substring(Math.max(0, providerId.length() - 4));
    }
}
