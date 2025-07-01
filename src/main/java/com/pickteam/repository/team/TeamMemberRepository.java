package com.pickteam.repository.team;

import com.pickteam.domain.team.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    
    /**
     * 팀과 사용자로 팀 멤버 찾기
     */
    Optional<TeamMember> findByTeamIdAndAccountId(Long teamId, Long accountId);
    
    /**
     * 팀의 활성 멤버 목록 조회
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.teamStatus = 'ACTIVE' AND tm.isDeleted = false")
    List<TeamMember> findActiveMembers(@Param("teamId") Long teamId);
    
    /**
     * 사용자가 속한 팀 목록 조회
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.account.id = :accountId AND tm.teamStatus = 'ACTIVE' AND tm.isDeleted = false")
    List<TeamMember> findByAccountId(@Param("accountId") Long accountId);
    
    /**
     * 팀장 찾기
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.teamRole = 'LEADER' AND tm.teamStatus = 'ACTIVE' AND tm.isDeleted = false")
    Optional<TeamMember> findTeamLeader(@Param("teamId") Long teamId);
    
    /**
     * 사용자가 특정 팀의 멤버인지 확인
     */
    @Query("SELECT COUNT(tm) > 0 FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.account.id = :accountId AND tm.teamStatus = 'ACTIVE' AND tm.isDeleted = false")
    boolean existsByTeamIdAndAccountId(@Param("teamId") Long teamId, @Param("accountId") Long accountId);
    
    /**
     * 사용자가 특정 팀의 팀장인지 확인
     */
    @Query("SELECT COUNT(tm) > 0 FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.account.id = :accountId AND tm.teamRole = 'LEADER' AND tm.teamStatus = 'ACTIVE' AND tm.isDeleted = false")
    boolean isTeamLeader(@Param("teamId") Long teamId, @Param("accountId") Long accountId);
} 