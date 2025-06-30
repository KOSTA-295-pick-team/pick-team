package com.pickteam.repository.team;

import com.pickteam.domain.team.Team;
import com.pickteam.domain.workspace.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {

    // 팀 이름으로 검색
    List<Team> findByNameContaining(String name);

    // 워크스페이스로 팀 조회
    List<Team> findByWorkspace(Workspace workspace);

    // 사용자 ID가 속한 팀 조회
    @Query("SELECT tm.team FROM TeamMember tm WHERE tm.account.id = :accountId")
    List<Team> findByAccountId(Long accountId);

}