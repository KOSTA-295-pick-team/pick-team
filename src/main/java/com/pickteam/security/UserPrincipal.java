package com.pickteam.security;

import com.pickteam.domain.user.Account;
import com.pickteam.domain.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security UserDetails 구현체
 * - Account 엔티티를 Spring Security에서 사용할 수 있도록 변환
 * - JWT 토큰 생성 및 인증 과정에서 사용자 정보 제공
 */
@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {
    private Long id;
    private String email;
    private String password;
    private UserRole role;
    private Collection<? extends GrantedAuthority> authorities;

    /** Account 엔티티로부터 UserPrincipal 객체 생성 */
    public static UserPrincipal create(Account account) {
        Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + account.getRole().name()));

        return new UserPrincipal(
                account.getId(),
                account.getEmail(),
                account.getPassword(),
                account.getRole(),
                authorities);
    }

    /** Spring Security에서 사용하는 username (이메일 사용) */
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /** 계정 만료 여부 (항상 유효) */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /** 계정 잠금 여부 (항상 잠금 해제) */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /** 자격 증명 만료 여부 (항상 유효) */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /** 계정 활성화 여부 (항상 활성화) */
    @Override
    public boolean isEnabled() {
        return true;
    }
}