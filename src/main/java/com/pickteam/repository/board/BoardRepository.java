package com.pickteam.repository.board;

import com.pickteam.domain.board.Board;
import com.pickteam.domain.team.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

    /**
     * ID로 활성 게시판 조회 (삭제되지 않은 게시판만)
     *
     * @param id 게시판 ID
     * @return 삭제되지 않은 게시판 (Optional)
     */
    Optional<Board> findByIdAndIsDeletedFalse(Long id);

    /**
     * 팀별 활성 게시판 목록 조회
     *
     * @param teamId 팀 ID
     * @return 해당 팀의 삭제되지 않은 게시판 목록
     */
    List<Board> findByTeamIdAndIsDeletedFalse(Long teamId);

    /**
     * 팀 정보와 함께 게시판 조회 (N+1 문제 해결)
     *
     * @param boardId 게시판 ID
     * @return 팀 정보가 페치 조인된 활성 게시판
     */
    @Query("SELECT b FROM Board b JOIN FETCH b.team WHERE b.id = :boardId AND b.isDeleted = false")
    Optional<Board> findByIdWithTeamAndIsDeletedFalse(@Param("boardId") Long boardId);
    
    /**
     * 특정 팀의 활성 게시판 조회
     *
     * @param team 팀 엔티티
     * @return 해당 팀의 삭제되지 않은 게시판 (Optional)
     */
    Optional<Board> findByTeamAndIsDeletedFalse(Team team);
}