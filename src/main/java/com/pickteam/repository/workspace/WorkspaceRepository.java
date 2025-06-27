package com.pickteam.repository.workspace;

import com.pickteam.domain.workspace.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    
    Optional<Workspace> findByUrl(String url);
    
    @Query("SELECT w FROM Workspace w JOIN w.members wm WHERE wm.account.id = :accountId AND w.isDeleted = false AND wm.status = 'ACTIVE'")
    List<Workspace> findByAccountId(@Param("accountId") Long accountId);
    
    List<Workspace> findByAccountIdAndIsDeletedFalse(Long accountId);
    
    Optional<Workspace> findByIdAndIsDeletedFalse(Long id);
} 