package com.pickteam.service.team;

import com.pickteam.domain.team.TeamMember;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamMemberService {
    /** 팀 멤버 추가* */
    public void addMember(Long teamId, Long accountId);

    /** 팀 멤버 추방 (추방된 팀원은 차단 상태가 아니면 다시 팀에 들어올 수 있다.)
     * 추방과 관련된 추가 로직이 붙을 수 있으므로 메소드 분리 (내부적으로 soft-delete 메소드 호출)
     * */
    public void banMember(Long teamId, Long memberId);

    /** 팀 멤버 삭제 (해당 멤버를 soft-delete처리) */
    public void deleteMember(Long teamId, Long memberId);

    /** 팀 멤버 차단*/
    public void blockMember(Long teamId, Long memberId);

    /** 팀 멤버 차단 해제*/
    public void unblockMember(Long teamId, Long memberId);


}
