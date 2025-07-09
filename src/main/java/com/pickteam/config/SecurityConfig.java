package com.pickteam.config;

import com.pickteam.security.JwtAuthenticationEntryPoint;
import com.pickteam.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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
                        // 인증 없이 접근 가능한 URL
                        .requestMatchers("/api/users/register", "/api/users/login", "/api/users/login/",
                                "/api/users/login/enhanced")
                        .permitAll()
                        .requestMatchers("/api/users/logout", "/api/users/logout/", "/api/users/logout/enhanced")
                        .permitAll()
                        .requestMatchers("/api/users/check-id", "/api/users/validate-password").permitAll()
                        .requestMatchers("/api/users/email/request", "/api/users/email/verify").permitAll()

                        // OAuth 인증 관련 경로 (인증 없이 접근 가능)
                        .requestMatchers("/api/auth/oauth/*/login", "/api/auth/oauth/*/callback").permitAll()
                        .requestMatchers("/api/auth/oauth/google/login", "/api/auth/oauth/google/callback").permitAll()
                        .requestMatchers("/api/auth/oauth/kakao/login", "/api/auth/oauth/kakao/callback").permitAll()

                        // 프로필 이미지는 공개 접근 허용
                        .requestMatchers("/profile-images/**").permitAll()

                        // 업로드 파일 직접 접근 차단 (보안 강화)
                        .requestMatchers("/uploads/**").denyAll()

                        // 파일 다운로드는 컨트롤러를 통해서만 허용
                        .requestMatchers("/api/files/*/download").authenticated()

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
