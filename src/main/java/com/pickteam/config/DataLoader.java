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
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final WorkspaceRepository workspaceRepository;
    private final TeamRepository teamRepository;
    private final BoardRepository boardRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 데이터가 이미 있으면 생성하지 않음
        if (accountRepository.count() > 0) {
            System.out.println("데이터가 이미 존재합니다. DataLoader를 건너뜁니다.");
            return;
        }

        System.out.println("=== DataLoader 시작 ===");

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

        admin = accountRepository.save(admin);
        user1 = accountRepository.save(user1);
        user2 = accountRepository.save(user2);

        entityManager.flush();
        System.out.println("사용자 생성 완료");

        // 2. 워크스페이스 생성
        Workspace workspace = Workspace.builder()
                .name("테스트 워크스페이스")
                .password("workspace123")
                .url("test-workspace")
                .account(admin)
                .build();

        workspace = workspaceRepository.save(workspace);
        entityManager.flush();
        System.out.println("워크스페이스 생성 완료 - ID: " + workspace.getId());

        // 3. 팀 생성
        Team team = Team.builder()
                .name("개발팀")
                .workspace(workspace)
                .build();

        team = teamRepository.save(team);
        entityManager.flush();
        System.out.println("팀 생성 완료 - ID: " + team.getId());

        // 4. 게시판 생성 - @SoftDelete 어노테이션이 자동으로 isDeleted = false 설정
        Board board1 = Board.builder()
                .team(team)
                .build();

        Board board2 = Board.builder()
                .team(team)
                .build();

        // @SoftDelete 어노테이션으로 자동 설정되므로 수동 설정 불필요
        board1 = boardRepository.save(board1);
        board2 = boardRepository.save(board2);
        entityManager.flush();

        System.out.println("게시판 생성 완료 - ID: " + board1.getId() + ", " + board2.getId());

        // 영속성 컨텍스트 클리어 후 재조회
        entityManager.clear();

        // 저장된 Board 검증
        Board verifyBoard1 = boardRepository.findById(board1.getId()).orElse(null);
        Board verifyBoard2 = boardRepository.findById(board2.getId()).orElse(null);

        System.out.println("=== DataLoader 완료 ===");
        System.out.println("사용자 ID: " + admin.getId() + "(관리자), " + user1.getId() + "(홍길동), " + user2.getId() + "(김철수)");
        System.out.println("워크스페이스 ID: " + workspace.getId());
        System.out.println("팀 ID: " + team.getId());
        System.out.println("게시판 ID: " + board1.getId() + ", " + board2.getId());

        // 검증 결과 출력
        if (verifyBoard1 != null) {
            System.out.println("Board 1 검증 성공 - ID: " + verifyBoard1.getId() + ", isDeleted: " + verifyBoard1.getIsDeleted());
        } else {
            System.err.println("Board 1 검증 실패!");
        }

        if (verifyBoard2 != null) {
            System.out.println("Board 2 검증 성공 - ID: " + verifyBoard2.getId() + ", isDeleted: " + verifyBoard2.getIsDeleted());
        } else {
            System.err.println("Board 2 검증 실패!");
        }
    }
}