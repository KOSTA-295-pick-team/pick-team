package com.pickteam.repository;

import com.pickteam.domain.workspace.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {
    
    boolean existsByWorkspaceIdAndAccountId(Long workspaceId, Long accountId);
    
    Optional<WorkspaceMember> findByWorkspaceIdAndAccountId(Long workspaceId, Long accountId);
    
    @Query("SELECT wm FROM WorkspaceMember wm WHERE wm.workspace.id = :workspaceId AND wm.status = 'ACTIVE' AND wm.isDeleted = false")
    List<WorkspaceMember> findActiveMembers(@Param("workspaceId") Long workspaceId);
    
    List<WorkspaceMember> findByAccountIdAndIsDeletedFalse(Long accountId);
} 