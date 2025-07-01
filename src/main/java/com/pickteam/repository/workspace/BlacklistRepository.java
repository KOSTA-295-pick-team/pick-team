package com.pickteam.repository.workspace;

import com.pickteam.domain.workspace.Blacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlacklistRepository extends JpaRepository<Blacklist, Long> {
    
    /**
     * 워크스페이스와 사용자로 블랙리스트 항목 찾기
     */
    Optional<Blacklist> findByWorkspaceIdAndAccountId(Long workspaceId, Long accountId);
    
    /**
     * 워크스페이스의 블랙리스트 목록 조회
     */
    @Query("SELECT b FROM Blacklist b WHERE b.workspace.id = :workspaceId AND b.isDeleted = false")
    List<Blacklist> findByWorkspaceId(@Param("workspaceId") Long workspaceId);
    
    /**
     * 사용자가 특정 워크스페이스에서 차단되었는지 확인
     */
    @Query("SELECT COUNT(b) > 0 FROM Blacklist b WHERE b.workspace.id = :workspaceId AND b.account.id = :accountId AND b.isDeleted = false")
    boolean existsByWorkspaceIdAndAccountId(@Param("workspaceId") Long workspaceId, @Param("accountId") Long accountId);
} 