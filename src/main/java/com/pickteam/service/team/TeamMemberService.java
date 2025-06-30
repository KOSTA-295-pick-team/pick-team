package com.pickteam.service.team;

public interface TeamMemberService {
    //팀 멤버 추가
    public void addMember(Long teamId, Long accountId);

    //팀 멤버 추방 (추방된 팀원은 차단 상태가 아니면 다시 팀에 들어올 수 있다.)
    public void banMember(Long teamId, Long memberId);

    //팀 멤버 차단
    public void blockMember(Long teamId, Long memberId);

    //팀 멤버 차단 해제
    public void unblockMember(Long teamId, Long memberId);


}
