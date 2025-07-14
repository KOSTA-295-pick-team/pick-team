package com.pickteam.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
@Profile("!test") // 테스트에서는 이 설정을 제외
public class JpaAuditingConfiguration {
}

