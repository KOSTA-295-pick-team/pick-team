package com.pickteam.repository.kanban;

import com.pickteam.domain.kanban.Kanban;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KanbanRepository extends JpaRepository<Kanban, Long> {
    
    @Query("SELECT k FROM Kanban k WHERE k.team.id = :teamId AND k.isDeleted = false")
    Optional<Kanban> findByTeamId(@Param("teamId") Long teamId);
    
    @Query("SELECT k FROM Kanban k WHERE k.workspace.id = :workspaceId AND k.isDeleted = false")
    List<Kanban> findByWorkspaceId(@Param("workspaceId") Long workspaceId);
    
    @Query("SELECT k FROM Kanban k WHERE k.team.id = :teamId AND k.workspace.id = :workspaceId AND k.isDeleted = false")
    Optional<Kanban> findByTeamIdAndWorkspaceId(@Param("teamId") Long teamId, @Param("workspaceId") Long workspaceId);
    
    @Query("SELECT k FROM Kanban k WHERE k.id = :id AND k.isDeleted = false")
    Optional<Kanban> findByIdAndIsDeletedFalse(@Param("id") Long id);
}