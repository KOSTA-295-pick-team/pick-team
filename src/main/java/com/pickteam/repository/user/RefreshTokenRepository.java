package com.pickteam.repository.user;

import com.pickteam.domain.user.RefreshToken;
import com.pickteam.domain.user.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

/**
 * JWT Refresh Token 리포지토리
 * - JWT 토큰 갱신을 위한 Refresh Token 데이터 액세스
 * - 토큰 유효성 검증 및 사용자별 토큰 관리
 * - 보안을 위한 토큰 삭제 및 정리 기능 제공
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * 토큰 문자열로 Refresh Token 조회
     * - JWT 토큰 갱신 시 토큰 유효성 검증에 사용
     * 
     * @param token 검색할 토큰 문자열
     * @return 해당 토큰의 RefreshToken 엔티티 (Optional)
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * 특정 사용자의 모든 Refresh Token 조회
     * - 사용자별 발급된 모든 토큰 관리
     * - 다중 디바이스 로그인 시 토큰 목록 확인
     * 
     * @param account 조회할 사용자 계정
     * @return 해당 사용자의 모든 RefreshToken 목록
     */
    List<RefreshToken> findByAccount(Account account);

    /**
     * 특정 사용자의 모든 Refresh Token 삭제
     * - 로그아웃 시 모든 디바이스에서 토큰 무효화
     * - 계정 삭제 시 관련 토큰 정리
     * - 보안 위험 시 강제 로그아웃 처리
     * 
     * @param account 토큰을 삭제할 사용자 계정
     */
    void deleteByAccount(Account account);
}
