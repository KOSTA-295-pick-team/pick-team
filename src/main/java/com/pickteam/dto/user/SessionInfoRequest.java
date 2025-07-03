package com.pickteam.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 세션 생성 시 클라이언트 정보 요청 DTO
 * - 로그인 시 디바이스 및 환경 정보 수집
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionInfoRequest {

    /** 디바이스 타입 (예: Mobile, Desktop, Tablet) */
    private String deviceType;

    /** 운영체제 정보 (예: Windows 10, Android 11, iOS 15) */
    private String operatingSystem;

    /** 브라우저 정보 (예: Chrome 95, Safari 15) */
    private String browser;

    /** 앱 버전 (모바일 앱인 경우) */
    private String appVersion;

    /** 추가 디바이스 정보 */
    private String additionalInfo;

    /**
     * 디바이스 정보를 문자열로 조합
     */
    public String toDeviceInfoString() {
        StringBuilder sb = new StringBuilder();

        if (deviceType != null && !deviceType.trim().isEmpty()) {
            sb.append(deviceType);
        }

        if (operatingSystem != null && !operatingSystem.trim().isEmpty()) {
            if (sb.length() > 0)
                sb.append(" | ");
            sb.append(operatingSystem);
        }

        if (browser != null && !browser.trim().isEmpty()) {
            if (sb.length() > 0)
                sb.append(" | ");
            sb.append(browser);
        }

        if (appVersion != null && !appVersion.trim().isEmpty()) {
            if (sb.length() > 0)
                sb.append(" | ");
            sb.append("v" + appVersion);
        }

        if (additionalInfo != null && !additionalInfo.trim().isEmpty()) {
            if (sb.length() > 0)
                sb.append(" | ");
            sb.append(additionalInfo);
        }

        return sb.length() > 0 ? sb.toString() : null;
    }
}
