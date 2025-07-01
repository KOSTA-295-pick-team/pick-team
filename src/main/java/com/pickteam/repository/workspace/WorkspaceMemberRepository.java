package com.pickteam.repository.workspace;

import com.pickteam.domain.workspace.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {
    
    /**
     * 워크스페이스 멤버 존재 여부 확인 (모든 상태 포함)
     */
    boolean existsByWorkspaceIdAndAccountId(Long workspaceId, Long accountId);
    
    /**
     * 활성 워크스페이스 멤버 존재 여부 확인
     */
    @Query("SELECT COUNT(wm) > 0 FROM WorkspaceMember wm WHERE wm.workspace.id = :workspaceId AND wm.account.id = :accountId AND wm.status = 'ACTIVE'")
    boolean existsActiveByWorkspaceIdAndAccountId(@Param("workspaceId") Long workspaceId, @Param("accountId") Long accountId);
    
    /**
     * 워크스페이스 멤버 찾기 (모든 상태 포함)
     */
    Optional<WorkspaceMember> findByWorkspaceIdAndAccountId(Long workspaceId, Long accountId);
    
    @Query("SELECT wm FROM WorkspaceMember wm WHERE wm.workspace.id = :workspaceId AND wm.status = 'ACTIVE'")
    List<WorkspaceMember> findActiveMembers(@Param("workspaceId") Long workspaceId);
    
    @Query("SELECT wm FROM WorkspaceMember wm WHERE wm.account.id = :accountId")
    List<WorkspaceMember> findByAccountId(@Param("accountId") Long accountId);
    
    @Query("SELECT wm FROM WorkspaceMember wm WHERE wm.account.id = :accountId AND wm.status = :status")
    List<WorkspaceMember> findByAccountIdAndStatus(@Param("accountId") Long accountId, @Param("status") WorkspaceMember.MemberStatus status);
} 