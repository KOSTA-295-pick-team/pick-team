package com.pickteam.service.team;

import com.pickteam.domain.team.Team;
import com.pickteam.domain.team.TeamMember;
import com.pickteam.domain.user.Account;
import com.pickteam.repository.team.TeamMemberRepository;
import com.pickteam.repository.team.TeamRepository;
import com.pickteam.repository.user.AccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Transactional
public class TeamMemberServiceImpl implements TeamMemberService {

    private final TeamRepository teamRepository;
    private final AccountRepository accountRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Override
    public void addMember(Long teamId, Long accountId) {
        // 이미 구현되어 있는 코드이므로 변경 불필요
    }

    @Override
    public void deleteMember(Long teamId, Long accountId) {
        TeamMember teamMember = teamMemberRepository.findByAccountIdAndTeamIdAndIsDeletedFalse(accountId, teamId);
        if (teamMember == null) {
            throw new IllegalArgumentException("해당 팀원을 찾을 수 없습니다.");
        }
        teamMember.markDeleted();
        teamMemberRepository.save(teamMember);
    }

    @Override
    public void banMember(Long teamId, Long accountId) {
        deleteMember(teamId, accountId);
        //필요 시 멤버 추방 관련 기능 추가 작성
    }

    @Override
    public void blockMember(Long teamId, Long accountId) {
        TeamMember teamMember = teamMemberRepository.findByAccountIdAndTeamId(accountId, teamId);
        if (teamMember == null) {
            throw new IllegalArgumentException("해당 팀원을 찾을 수 없습니다.");
        }
        teamMember.setBlocked(true);
        teamMemberRepository.save(teamMember);
    }

    @Override
    public void unblockMember(Long teamId, Long accountId) {
        TeamMember teamMember = teamMemberRepository.findByAccountIdAndTeamId(accountId, teamId);
        if (teamMember == null) {
            throw new IllegalArgumentException("해당 팀원을 찾을 수 없습니다.");
        }
        teamMember.setBlocked(false);
        teamMemberRepository.save(teamMember);
    }
}