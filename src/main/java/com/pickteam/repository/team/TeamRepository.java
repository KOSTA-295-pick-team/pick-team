package com.pickteam.repository.team;

import com.pickteam.domain.team.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    
    /**
     * 워크스페이스의 활성 팀 목록 조회
     */
    @Query("SELECT t FROM Team t WHERE t.workspace.id = :workspaceId AND t.isDeleted = false")
    List<Team> findByWorkspaceId(@Param("workspaceId") Long workspaceId);
    
    /**
     * 삭제되지 않은 팀 조회
     */
    Optional<Team> findByIdAndIsDeletedFalse(Long id);
}