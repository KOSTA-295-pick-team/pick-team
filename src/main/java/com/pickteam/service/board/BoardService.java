package com.pickteam.service.board;

import com.pickteam.domain.board.Board;
import com.pickteam.domain.team.Team;
import com.pickteam.repository.board.BoardRepository;
import com.pickteam.repository.team.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 게시판 서비스
 * 팀별 게시판 생성, 조회, 관리를 담당합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BoardService {

    private final BoardRepository boardRepository;
    private final TeamRepository teamRepository;

    /**
     * 팀 생성 시 자동으로 게시판 생성
     * 
     * @param teamId 팀 ID
     * @return 생성된 게시판
     */
    @Transactional
    public Board createDefaultBoardForTeam(Long teamId) {
        Team team = teamRepository.findByIdAndIsDeletedFalse(teamId)
                .orElseThrow(() -> new RuntimeException("팀을 찾을 수 없습니다. teamId: " + teamId));

        // 이미 게시판이 존재하는지 확인
        Optional<Board> existingBoard = boardRepository.findByTeamIdAndIsDeletedFalse(teamId)
                .stream()
                .findFirst();

        if (existingBoard.isPresent()) {
            log.info("팀 ID {}에 대한 게시판이 이미 존재합니다. boardId: {}", teamId, existingBoard.get().getId());
            return existingBoard.get();
        }

        // 새 게시판 생성
        Board board = Board.builder()
                .team(team)
                .build();

        board = boardRepository.save(board);
        log.info("팀 ID {}에 대한 새 게시판이 생성되었습니다. boardId: {}", teamId, board.getId());
        
        return board;
    }

    /**
     * 팀 ID로 게시판 조회 (없으면 자동 생성)
     * 
     * @param teamId 팀 ID
     * @return 게시판
     */
    @Transactional
    public Board getBoardByTeamId(Long teamId) {
        Optional<Board> boardOpt = boardRepository.findByTeamIdAndIsDeletedFalse(teamId)
                .stream()
                .findFirst();

        if (boardOpt.isPresent()) {
            return boardOpt.get();
        }

        // 게시판이 없으면 자동으로 생성
        log.info("팀 ID {}에 대한 게시판이 없어서 자동으로 생성합니다.", teamId);
        return createDefaultBoardForTeam(teamId);
    }

    /**
     * 게시판 ID로 조회
     * 
     * @param boardId 게시판 ID
     * @return 게시판
     */
    public Board getBoardById(Long boardId) {
        return boardRepository.findByIdAndIsDeletedFalse(boardId)
                .orElseThrow(() -> new RuntimeException("게시판을 찾을 수 없습니다. boardId: " + boardId));
    }

    /**
     * 게시판 삭제 (Soft Delete)
     * 
     * @param boardId 게시판 ID
     */
    @Transactional
    public void deleteBoard(Long boardId) {
        Board board = getBoardById(boardId);
        board.markDeleted();
        boardRepository.save(board);
        log.info("게시판이 삭제되었습니다. boardId: {}", boardId);
    }
}
