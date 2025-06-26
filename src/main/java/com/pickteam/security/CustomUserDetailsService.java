package com.pickteam.security;

import com.pickteam.domain.user.Account;
import com.pickteam.repository.user.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 사용자 정보 조회 서비스
 * - Spring Security 인증 과정에서 사용자 정보를 로드
 * - 이메일 또는 사용자 ID로 계정 조회
 * - Soft Delete된 계정 제외
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    /** 이메일로 사용자 정보 조회 (Spring Security 기본 메서드) */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        return UserPrincipal.create(account);
    }

    /** 사용자 ID로 사용자 정보 조회 (JWT 토큰 검증 시 사용) */
    public UserDetails loadUserById(Long id) {
        Account account = accountRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + id));

        return UserPrincipal.create(account);
    }
}
