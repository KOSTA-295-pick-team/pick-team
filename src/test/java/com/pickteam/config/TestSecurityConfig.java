package com.pickteam.config;

import com.pickteam.domain.enums.UserRole;
import com.pickteam.security.UserPrincipal;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

/**
 * 테스트용 Security 설정
 * Mock 인증 사용자를 제공하여 Controller 테스트 가능
 */
@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    @Primary
    public UserDetailsService testUserDetailsService() {
        // 테스트용 기본 사용자 생성
        UserPrincipal testUser = new UserPrincipal(
                1L,
                "test@example.com",
                "테스트 사용자",
                "password",
                UserRole.USER,
                List.of()
        );

        return username -> testUser;
    }
}