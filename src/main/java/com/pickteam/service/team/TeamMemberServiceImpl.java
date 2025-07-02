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
        if (teamMemberRepository.existsByTeamIdAndAccountIdAndIsDeletedFalse(teamId, accountId)) {
            throw new IllegalStateException("이미 팀에 속해있는 회원입니다.");
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀입니다."));
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        if (teamMemberRepository.existsByTeamIdAndAccountIdAndBlockedTrue(teamId, accountId)) {
            throw new IllegalStateException("차단된 회원은 팀에 가입할 수 없습니다.");
        }

        TeamMember teamMember = TeamMember.builder()
                .team(team)
                .account(account)
                .isBlocked(false)
                .build();

        teamMemberRepository.save(teamMember);
    }

    @Override
    public void deleteMember(Long teamId, Long memberId) {
        TeamMember teamMember = teamMemberRepository.findByAccountIdAndTeamIdAndIsDeletedFalse(memberId, teamId);
        if (teamMember == null) {
            throw new IllegalArgumentException("해당 팀원을 찾을 수 없습니다.");
        }

        teamMember.markDeleted();
        teamMemberRepository.save(teamMember);
    }

    @Override
    public void banMember(Long teamId, Long memberId) {
        deleteMember(teamId, memberId);
    }

    @Override
    public void blockMember(Long teamId, Long memberId) {
        TeamMember teamMember = teamMemberRepository.findByAccountIdAndTeamId(memberId, teamId);
        if (teamMember == null) {
            throw new IllegalArgumentException("해당 팀원을 찾을 수 없습니다.");
        }

        teamMember.setBlocked(true);
        teamMemberRepository.save(teamMember);
    }

    @Override
    public void unblockMember(Long teamId, Long memberId) {
        TeamMember teamMember = teamMemberRepository.findByAccountIdAndTeamId(memberId, teamId);
        if (teamMember == null) {
            throw new IllegalArgumentException("해당 팀원을 찾을 수 없습니다.");
        }

        teamMember.setBlocked(false);
        // 차단 해제 시 복구도 함께 수행
        teamMember.restore();
        teamMemberRepository.save(teamMember);
    }
}