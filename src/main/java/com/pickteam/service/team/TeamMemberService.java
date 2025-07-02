package com.pickteam.service.team;

public interface TeamMemberService {
    /** 팀 멤버 추가*/
    void addMember(Long teamId, Long accountId);

    /** 팀 멤버 삭제*/
    void deleteMember(Long teamId, Long accountId);

    /** 팀 멤버 추방 (추방된 팀원은 차단 상태가 아니면 다시 팀에 들어올 수 있다.)
     * 추방과 관련된 추가 로직이 붙을 수 있으므로 메소드 분리 (내부적으로 soft-delete 메소드 호출)
     * */
    void banMember(Long teamId, Long accountId);

    /** 팀 멤버 차단 (재가입 불가능 처리)*/
    void blockMember(Long teamId, Long accountId);

    /** 팀 멤버 차단 해제 */
    void unblockMember(Long teamId, Long accountId);
}