package com.pickteam.repository.workspace;

import com.pickteam.domain.workspace.Workspace;
import com.pickteam.domain.workspace.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    
    Optional<Workspace> findByUrl(String url);
    
    @Query("SELECT DISTINCT w FROM Workspace w JOIN w.members wm JOIN wm.account a WHERE a.id = :accountId AND w.isDeleted = false AND wm.status = :status")
    List<Workspace> findByAccountId(@Param("accountId") Long accountId, @Param("status") WorkspaceMember.MemberStatus status);
    
    List<Workspace> findByAccountIdAndIsDeletedFalse(Long accountId);
    
    Optional<Workspace> findByIdAndIsDeletedFalse(Long id);
} 