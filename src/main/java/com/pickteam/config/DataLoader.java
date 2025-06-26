package com.pickteam.config;

import com.pickteam.domain.board.Board;
import com.pickteam.domain.enums.UserRole;
import com.pickteam.domain.team.Team;
import com.pickteam.domain.user.Account;
import com.pickteam.domain.workspace.Workspace;
import com.pickteam.repository.board.BoardRepository;
import com.pickteam.repository.team.TeamRepository;
import com.pickteam.repository.user.AccountRepository;
import com.pickteam.repository.workspace.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final WorkspaceRepository workspaceRepository;
    private final TeamRepository teamRepository;
    private final BoardRepository boardRepository;

    @Override
    public void run(String... args) throws Exception {
        // 데이터가 이미 있으면 생성하지 않음
        if (accountRepository.count() > 0) {
            return;
        }

        // 1. 사용자 생성
        Account admin = Account.builder()
                .email("admin@example.com")
                .password("password123")
                .name("관리자")
                .age(30)
                .role(UserRole.ADMIN)
                .mbti("INTJ")
                .disposition("차분함")
                .introduction("안녕하세요, 관리자입니다.")
                .portfolio("https://github.com/admin")
                .preferWorkstyle("계획적인 업무")
                .dislikeWorkstyle("즉흥적인 업무")
                .build();

        Account user1 = Account.builder()
                .email("user1@example.com")
                .password("password123")
                .name("홍길동")
                .age(25)
                .role(UserRole.USER)
                .mbti("ENFP")
                .disposition("활발함")
                .introduction("안녕하세요, 홍길동입니다.")
                .portfolio("https://github.com/user1")
                .preferWorkstyle("자유로운 분위기")
                .dislikeWorkstyle("경직된 분위기")
                .build();

        Account user2 = Account.builder()
                .email("user2@example.com")
                .password("password123")
                .name("김철수")
                .age(28)
                .role(UserRole.USER)
                .mbti("ISFJ")
                .disposition("성실함")
                .introduction("안녕하세요, 김철수입니다.")
                .portfolio("https://github.com/user2")
                .preferWorkstyle("체계적인 업무")
                .dislikeWorkstyle("무질서한 환경")
                .build();

        accountRepository.save(admin);
        accountRepository.save(user1);
        accountRepository.save(user2);

        // 2. 워크스페이스 생성
        Workspace workspace = Workspace.builder()
                .name("테스트 워크스페이스")
                .password("workspace123")
                .url("test-workspace")
                .account(admin)
                .build();

        workspaceRepository.save(workspace);

        // 3. 팀 생성
        Team team = Team.builder()
                .name("개발팀")
                .workspace(workspace)
                .build();

        teamRepository.save(team);

        // 4. 게시판 생성
        Board board1 = Board.builder()
                .team(team)
                .build();

        Board board2 = Board.builder()
                .team(team)
                .build();

        boardRepository.save(board1);
        boardRepository.save(board2);

        System.out.println("초기 데이터가 생성되었습니다.");
        System.out.println("사용자 ID: 1(관리자), 2(홍길동), 3(김철수)");
        System.out.println("워크스페이스 ID: 1");
        System.out.println("팀 ID: 1");
        System.out.println("게시판 ID: 1, 2");
    }
}