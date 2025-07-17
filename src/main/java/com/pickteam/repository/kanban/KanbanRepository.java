package com.pickteam.repository.kanban;

import com.pickteam.domain.kanban.Kanban;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KanbanRepository extends JpaRepository<Kanban, Long> {
    
    // 팀의 모든 칸반 조회 (확장성 고려)
    @Query("SELECT k FROM Kanban k WHERE k.team.id = :teamId AND k.isDeleted = false ORDER BY k.order ASC, k.createdAt ASC")
    List<Kanban> findByTeamId(@Param("teamId") Long teamId);
    
    // 첫 번째 칸반 조회 (현재 요구사항용) - Pageable 사용
    @Query("SELECT k FROM Kanban k WHERE k.team.id = :teamId AND k.isDeleted = false ORDER BY k.order ASC, k.createdAt ASC")
    List<Kanban> findFirstByTeamId(@Param("teamId") Long teamId, Pageable pageable);
    
    @Query("SELECT k FROM Kanban k WHERE k.workspace.id = :workspaceId AND k.isDeleted = false ORDER BY k.order ASC")
    List<Kanban> findByWorkspaceId(@Param("workspaceId") Long workspaceId);
    
    @Query("SELECT k FROM Kanban k WHERE k.team.id = :teamId AND k.workspace.id = :workspaceId AND k.isDeleted = false ORDER BY k.order ASC")
    List<Kanban> findByTeamIdAndWorkspaceId(@Param("teamId") Long teamId, @Param("workspaceId") Long workspaceId);
    
    @Query("SELECT k FROM Kanban k WHERE k.id = :id AND k.isDeleted = false")
    Optional<Kanban> findByIdAndIsDeletedFalse(@Param("id") Long id);
    
    // 칸반 존재 여부 확인
    @Query("SELECT COUNT(k) > 0 FROM Kanban k WHERE k.team.id = :teamId AND k.isDeleted = false")
    boolean existsByTeamId(@Param("teamId") Long teamId);
}