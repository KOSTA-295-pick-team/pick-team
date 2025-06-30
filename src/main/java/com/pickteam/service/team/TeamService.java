package com.pickteam.service.team;

import com.pickteam.domain.team.Team;
import com.pickteam.domain.workspace.Workspace;
import com.pickteam.dto.team.TeamResponse;

import java.util.List;

public interface TeamService {

    // 팀 id로 팀 가져오기 (팀 정보 표시용)
    public TeamResponse getTeamById(Long id);

    // 팀 id로 팀 가져오기 (내부 업데이트용)
    public Team getTeamByIdForUpdate(Long id);

    // 팀 이름으로 팀 검색하기
    public List<TeamResponse> searchByName(String name);

    // 사용자 id가 속한 팀 목록 가져오기
    public List<TeamResponse> getTeamsByAccount(Long accountId);

    // 팀 생성
    public TeamResponse createTeam(String name, Long accountId, Workspace workspace);

    // 팀 삭제 (Soft Delete)
    public void deleteTeam(Long id) ;

    // 팀 이름 변경
    public TeamResponse updateTeamName(Long id, String newName) ;

    //팀 비밀번호 설정
    public TeamResponse updateTeamPassword(Long id, String newPassword) ;

    //팀 비밀번호 제거 (초기 상태로)
    public TeamResponse removeTeamPassword(Long id);

    // 게시판 목록 가져오기
    public List<?> getBoards(Long teamId) ;

    // 칸반 목록 가져오기
    public List<?> getKanbans(Long teamId) ;

    // 팀원 목록 가져오기
    public List<?> getTeamMembers(Long teamId);



}
