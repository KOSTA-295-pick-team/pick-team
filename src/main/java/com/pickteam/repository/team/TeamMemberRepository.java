package com.pickteam.repository.team;

import com.pickteam.domain.team.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    /** 팀 Id 및 계정 Id로 유효성 검사 */
    boolean existsByTeamIdAndAccountIdAndIsDeletedFalse(Long teamId, Long accountId);

    /** 팀원 조회 (삭제된 항목 포함)*/
    TeamMember findByAccountIdAndTeamId(Long accountId, Long teamId);

    /** 팀원 조회 * */
    TeamMember findByAccountIdAndTeamIdAndIsDeletedFalse(Long accountId, Long teamId);

    /** 팀에 속한 팀원 조회 (삭제된 항목 포함) */
    List<TeamMember> findByTeamId(Long teamId);

    /** 이름으로 팀원 조회 */
    @Query("SELECT tm FROM TeamMember tm JOIN tm.account a WHERE a.name LIKE %:name% AND tm.isDeleted = false")
    List<TeamMember> findByAccountNameContaining(@Param("name") String name);

    /** 팀 내에서 이름으로 팀원 조회 */
    @Query("SELECT tm FROM TeamMember tm JOIN tm.account a WHERE tm.team.id = :teamId AND a.name LIKE %:name% AND tm.isDeleted = false")
    List<TeamMember> findByTeamIdAndAccountNameContaining(@Param("teamId") Long teamId, @Param("name") String name);

    /** 팀의 차단된 멤버 조회 */
    List<TeamMember> findByTeamIdAndBlockedTrue(Long teamId);

    /** 계정이 속한 팀 조회 */
    List<TeamMember> findByAccountIdAndIsDeletedFalse(Long accountId);

    /** 특정 팀에서 계정의 차단 여부 확인 */
    boolean existsByTeamIdAndAccountIdAndBlockedTrue(Long teamId, Long accountId);


}
