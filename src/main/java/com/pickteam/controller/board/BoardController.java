package com.pickteam.controller.board;

import com.pickteam.domain.board.Board;
import com.pickteam.dto.board.BoardResponseDto;
import com.pickteam.service.board.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 게시판 컨트롤러
 * 팀별 게시판 조회 및 관리 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/teams/{teamId}/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    /**
     * 팀의 게시판 정보 조회 (없으면 자동 생성)
     */
    @GetMapping
    public ResponseEntity<BoardResponseDto> getTeamBoard(@PathVariable Long teamId) {
        Board board = boardService.getBoardByTeamId(teamId);
        BoardResponseDto response = BoardResponseDto.builder()
                .id(board.getId())
                .teamId(board.getTeam().getId())
                .teamName(board.getTeam().getName())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * 게시판 ID로 직접 조회
     */
    @GetMapping("/{boardId}")
    public ResponseEntity<BoardResponseDto> getBoardById(
            @PathVariable Long teamId,
            @PathVariable Long boardId) {
        
        Board board = boardService.getBoardById(boardId);
        BoardResponseDto response = BoardResponseDto.builder()
                .id(board.getId())
                .teamId(board.getTeam().getId())
                .teamName(board.getTeam().getName())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
        
        return ResponseEntity.ok(response);
    }
}
