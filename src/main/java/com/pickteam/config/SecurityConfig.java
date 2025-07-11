package com.pickteam.config;

import com.pickteam.security.JwtAuthenticationEntryPoint;
import com.pickteam.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (JWT 사용시)
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 세션 비활성화 (JWT 사용시)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 예외 처리
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))

                // URL 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 정적 리소스 (인증 없이 접근 가능)
                        .requestMatchers("/favicon.ico", "/error", "/actuator/health",
                                "/.well-known/**", "/robots.txt", "/sitemap.xml")
                        .permitAll()

                        // 회원가입 관련 API (인증 불필요)
                        .requestMatchers("/api/users/register", "/api/users/check-id", "/api/users/validate-password")
                        .permitAll()

                        // 인증 관련 API - AuthController (인증 불필요)
                        .requestMatchers("/api/auth/**").permitAll()

                        // 프로필 이미지는 공개 접근 허용
                        .requestMatchers("/profile-images/**").permitAll()

                        // 업로드 파일 직접 접근 차단 (보안 강화)
                        .requestMatchers("/uploads/**").denyAll()

                        // 파일 다운로드는 컨트롤러를 통해서만 허용 (인증 필요)
                        .requestMatchers("/api/files/*/download").authenticated()

                        // 전체 사용자 프로필 조회는 공개 (팀 매칭용)
                        .requestMatchers("/api/users").permitAll()

                        // 특정 사용자 프로필 조회는 공개 (팀 매칭용)
                        .requestMatchers(HttpMethod.GET, "/api/users/{userId}").permitAll()

                        .requestMatchers("/api/users/email/request", "/api/users/email/verify").permitAll()
                        //웹소켓 서버 접속 요청에 대해 permitall설정
                        .requestMatchers("/ws").permitAll()
                        
                        //livekit 서버에서 전송하는 hook 메시지에 대해 permitall설정
                        .requestMatchers(new RegexRequestMatcher("/api/workspaces/\\d+/video-channels/livekit/webhooks",null)).permitAll()
                        
                        // ADMIN 권한 필요
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 나머지는 인증 필요
                        .anyRequest().authenticated())

                // JWT 필터 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 나중에 환경변수로 설정 예정
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}
