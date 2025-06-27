package com.pickteam.config;

import com.pickteam.domain.user.Account;
import com.pickteam.repository.account.AccountRepository;
import com.pickteam.domain.enums.UserRole;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // 테스트용 사용자 3명 생성
        if (accountRepository.count() == 0) {
            createTestUser("test1@example.com", "테스트유저1", "password");
            createTestUser("test2@example.com", "테스트유저2", "password");
            createTestUser("test3@example.com", "테스트유저3", "password");
            
            System.out.println("테스트 사용자 3명이 생성되었습니다.");
            System.out.println("test1@example.com / password");
            System.out.println("test2@example.com / password");
            System.out.println("test3@example.com / password");
        }
    }
    
    private void createTestUser(String email, String name, String password) {
        Account account = Account.builder()
                .email(email)
                .name(name)
                .password(passwordEncoder.encode(password))
                .age(25)
                .role(UserRole.USER)
                .mbti("ENFP")
                .introduction("테스트 사용자입니다. 팀 협업을 좋아합니다.")
                .disposition("적극적이고 협력적인 성향입니다.")
                .build();
        
        accountRepository.save(account);
    }
} 