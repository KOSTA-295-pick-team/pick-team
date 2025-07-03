package com.pickteam.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * HTTP 요청에서 클라이언트 정보를 추출하는 유틸리티
 */
public class ClientInfoExtractor {

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    /**
     * 클라이언트 IP 주소 추출
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        for (String header : IP_HEADER_CANDIDATES) {
            String ipList = request.getHeader(header);
            if (StringUtils.hasText(ipList) && !"unknown".equalsIgnoreCase(ipList)) {
                // 첫 번째 IP 주소 사용 (프록시를 거친 경우 여러 IP가 있을 수 있음)
                return ipList.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * User-Agent 헤더에서 브라우저 정보 추출
     */
    public static String getBrowserInfo(String userAgent) {
        if (!StringUtils.hasText(userAgent)) {
            return "Unknown Browser";
        }

        // 간단한 브라우저 감지 로직
        if (userAgent.contains("Chrome") && !userAgent.contains("Edg")) {
            return "Chrome";
        } else if (userAgent.contains("Firefox")) {
            return "Firefox";
        } else if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) {
            return "Safari";
        } else if (userAgent.contains("Edg")) {
            return "Edge";
        } else if (userAgent.contains("Opera") || userAgent.contains("OPR")) {
            return "Opera";
        } else {
            return "Other";
        }
    }

    /**
     * User-Agent 헤더에서 운영체제 정보 추출
     */
    public static String getOperatingSystem(String userAgent) {
        if (!StringUtils.hasText(userAgent)) {
            return "Unknown OS";
        }

        if (userAgent.contains("Windows NT 10.0")) {
            return "Windows 10";
        } else if (userAgent.contains("Windows NT 6.3")) {
            return "Windows 8.1";
        } else if (userAgent.contains("Windows NT 6.2")) {
            return "Windows 8";
        } else if (userAgent.contains("Windows NT 6.1")) {
            return "Windows 7";
        } else if (userAgent.contains("Windows")) {
            return "Windows";
        } else if (userAgent.contains("Mac OS X")) {
            return "macOS";
        } else if (userAgent.contains("Android")) {
            return "Android";
        } else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            return "iOS";
        } else if (userAgent.contains("Linux")) {
            return "Linux";
        } else {
            return "Other";
        }
    }

    /**
     * User-Agent 헤더에서 디바이스 타입 추출
     */
    public static String getDeviceType(String userAgent) {
        if (!StringUtils.hasText(userAgent)) {
            return "Unknown";
        }

        if (userAgent.contains("Mobile") || userAgent.contains("Android")) {
            return "Mobile";
        } else if (userAgent.contains("Tablet") || userAgent.contains("iPad")) {
            return "Tablet";
        } else {
            return "Desktop";
        }
    }

    /**
     * User-Agent에서 간단한 디바이스 정보 문자열 생성
     */
    public static String getSimpleDeviceInfo(String userAgent) {
        if (!StringUtils.hasText(userAgent)) {
            return "Unknown Device";
        }

        String deviceType = getDeviceType(userAgent);
        String os = getOperatingSystem(userAgent);
        String browser = getBrowserInfo(userAgent);

        return String.format("%s | %s | %s", deviceType, os, browser);
    }

    /**
     * 요청에서 완전한 클라이언트 정보 추출
     */
    public static ClientInfo extractClientInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = getClientIpAddress(request);

        return ClientInfo.builder()
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .deviceType(getDeviceType(userAgent))
                .operatingSystem(getOperatingSystem(userAgent))
                .browser(getBrowserInfo(userAgent))
                .build();
    }

    /**
     * 클라이언트 정보를 담는 내부 클래스
     */
    public static class ClientInfo {
        private String ipAddress;
        private String userAgent;
        private String deviceType;
        private String operatingSystem;
        private String browser;

        public static ClientInfoBuilder builder() {
            return new ClientInfoBuilder();
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public String getUserAgent() {
            return userAgent;
        }

        public String getDeviceType() {
            return deviceType;
        }

        public String getOperatingSystem() {
            return operatingSystem;
        }

        public String getBrowser() {
            return browser;
        }

        public String getDeviceInfoString() {
            return String.format("%s | %s | %s", deviceType, operatingSystem, browser);
        }

        public static class ClientInfoBuilder {
            private String ipAddress;
            private String userAgent;
            private String deviceType;
            private String operatingSystem;
            private String browser;

            public ClientInfoBuilder ipAddress(String ipAddress) {
                this.ipAddress = ipAddress;
                return this;
            }

            public ClientInfoBuilder userAgent(String userAgent) {
                this.userAgent = userAgent;
                return this;
            }

            public ClientInfoBuilder deviceType(String deviceType) {
                this.deviceType = deviceType;
                return this;
            }

            public ClientInfoBuilder operatingSystem(String operatingSystem) {
                this.operatingSystem = operatingSystem;
                return this;
            }

            public ClientInfoBuilder browser(String browser) {
                this.browser = browser;
                return this;
            }

            public ClientInfo build() {
                ClientInfo clientInfo = new ClientInfo();
                clientInfo.ipAddress = this.ipAddress;
                clientInfo.userAgent = this.userAgent;
                clientInfo.deviceType = this.deviceType;
                clientInfo.operatingSystem = this.operatingSystem;
                clientInfo.browser = this.browser;
                return clientInfo;
            }
        }
    }
}
