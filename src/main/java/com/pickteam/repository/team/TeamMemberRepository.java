package com.pickteam.repository.team;

import com.pickteam.domain.team.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;

import java.util.List;

interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<TeamMember> findByTeamId(Long teamId);
    boolean existsByTeamIdAndAccountId(Long teamId, Long accountId);

}
