package com.pickteam.config;

import com.pickteam.domain.user.Account;
import com.pickteam.domain.workspace.Workspace;
import com.pickteam.domain.workspace.WorkspaceMember;
import com.pickteam.domain.team.Team;
import com.pickteam.domain.team.TeamMember;
import com.pickteam.domain.kanban.Kanban;
import com.pickteam.domain.kanban.KanbanList;
import com.pickteam.domain.kanban.KanbanTask;
import com.pickteam.domain.kanban.KanbanTaskMember;
import com.pickteam.repository.user.AccountRepository;
import com.pickteam.repository.workspace.WorkspaceRepository;
import com.pickteam.repository.workspace.WorkspaceMemberRepository;
import com.pickteam.repository.team.TeamRepository;
import com.pickteam.repository.team.TeamMemberRepository;
import com.pickteam.repository.kanban.KanbanRepository;
import com.pickteam.repository.kanban.KanbanListRepository;
import com.pickteam.repository.kanban.KanbanTaskRepository;
import com.pickteam.repository.kanban.KanbanTaskMemberRepository;
import com.pickteam.domain.enums.UserRole;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final AccountRepository accountRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final KanbanRepository kanbanRepository;
    private final KanbanListRepository kanbanListRepository;
    private final KanbanTaskRepository kanbanTaskRepository;
    private final KanbanTaskMemberRepository kanbanTaskMemberRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // 테스트용 사용자 3명 생성
        if (accountRepository.count() == 0) {
            Account user1 = createTestUser("test1@example.com", "테스트유저1", "password");
            Account user2 = createTestUser("test2@example.com", "테스트유저2", "password");
            Account user3 = createTestUser("test3@example.com", "테스트유저3", "password");
            
            System.out.println("테스트 사용자 3명이 생성되었습니다:");
            System.out.println("test1@example.com / password");
            System.out.println("test2@example.com / password");
            System.out.println("test3@example.com / password");
        }
        
        // 워크스페이스가 없으면 테스트 워크스페이스 생성
        if (workspaceRepository.count() == 0) {
            Account user1 = accountRepository.findByEmail("test1@example.com").orElse(null);
            Account user2 = accountRepository.findByEmail("test2@example.com").orElse(null);
            Account user3 = accountRepository.findByEmail("test3@example.com").orElse(null);
            
            if (user1 != null && user2 != null && user3 != null) {
                // 테스트용 워크스페이스 생성
                Workspace workspace1 = createTestWorkspace("테스트 워크스페이스 1", user1);
                Workspace workspace2 = createTestWorkspace("개발팀 워크스페이스", user2);
                
                // 워크스페이스 멤버십 생성
                WorkspaceMember member1 = createWorkspaceMembership(workspace1, user1, WorkspaceMember.MemberRole.OWNER);
                WorkspaceMember member2 = createWorkspaceMembership(workspace1, user2, WorkspaceMember.MemberRole.MEMBER);
                WorkspaceMember member3 = createWorkspaceMembership(workspace1, user3, WorkspaceMember.MemberRole.MEMBER);
                
                createWorkspaceMembership(workspace2, user2, WorkspaceMember.MemberRole.OWNER);
                createWorkspaceMembership(workspace2, user1, WorkspaceMember.MemberRole.MEMBER);
                
                System.out.println("테스트 워크스페이스가 생성되었습니다:");
                System.out.println("- 테스트 워크스페이스 1 (test1@example.com 소유)");
                System.out.println("- 개발팀 워크스페이스 (test2@example.com 소유)");
                
                // 팀 테스트 데이터 생성
                createTestTeams(workspace1, user1, user2, user3);
                
                System.out.println("테스트 팀과 칸반 보드가 생성되었습니다:");
                System.out.println("- 프론트엔드팀 (test1@example.com 팀장) + 칸반 보드");
                System.out.println("- 백엔드팀 (test2@example.com 팀장) + 칸반 보드");
                System.out.println("- 샘플 태스크들이 각 팀의 칸반 보드에 추가되었습니다.");
            }
        }
    }
    
    private Account createTestUser(String email, String name, String password) {
        Account account = Account.builder()
                .email(email)
                .name(name)
                .password(passwordEncoder.encode(password))
                .age(25)
                .role(UserRole.USER)
                .mbti("ENFP")
                .introduction("테스트 사용자입니다. 팀 협업을 좋아합니다.")
                .disposition("적극적이고 협력적인 성향입니다.")
                .build();
        
        return accountRepository.save(account);
    }
    
    private Workspace createTestWorkspace(String name, Account owner) {
        Workspace workspace = Workspace.builder()
                .name(name)
                .iconUrl("https://picsum.photos/seed/" + name.hashCode() + "/100/100")
                .account(owner)
                .url(generateRandomCode())
                .build();
        
        return workspaceRepository.save(workspace);
    }
    
    private WorkspaceMember createWorkspaceMembership(Workspace workspace, Account account, WorkspaceMember.MemberRole role) {
        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(workspace)
                .account(account)
                .role(role)
                .status(WorkspaceMember.MemberStatus.ACTIVE)
                .build();
        
        return workspaceMemberRepository.save(member);
    }
    
    private void createTestTeams(Workspace workspace, Account user1, Account user2, Account user3) {
        // 프론트엔드팀 생성 (user1이 팀장)
        Team frontendTeam = Team.builder()
                .name("프론트엔드팀")
                .workspace(workspace)
                .build();
        frontendTeam = teamRepository.save(frontendTeam);
        
        // 프론트엔드팀 멤버십 생성
        createTeamMembership(frontendTeam, user1, TeamMember.TeamRole.LEADER);
        createTeamMembership(frontendTeam, user3, TeamMember.TeamRole.MEMBER);
        
        // 프론트엔드팀 칸반 보드 생성
        createKanbanBoardForTeam(frontendTeam, workspace, user1, user3);
        
        // 백엔드팀 생성 (user2가 팀장)
        Team backendTeam = Team.builder()
                .name("백엔드팀")
                .workspace(workspace)
                .build();
        backendTeam = teamRepository.save(backendTeam);
        
        // 백엔드팀 멤버십 생성
        createTeamMembership(backendTeam, user2, TeamMember.TeamRole.LEADER);
        createTeamMembership(backendTeam, user1, TeamMember.TeamRole.MEMBER);
        createTeamMembership(backendTeam, user3, TeamMember.TeamRole.MEMBER);
        
        // 백엔드팀 칸반 보드 생성
        createKanbanBoardForTeam(backendTeam, workspace, user1, user2, user3);
    }
    
    private void createTeamMembership(Team team, Account account, TeamMember.TeamRole role) {
        TeamMember member = TeamMember.builder()
                .team(team)
                .account(account)
                .teamRole(role)
                .teamStatus(TeamMember.TeamStatus.ACTIVE)
                .build();
        
        teamMemberRepository.save(member);
    }
    
    private void createKanbanBoardForTeam(Team team, Workspace workspace, Account... members) {
        // 칸반 보드 생성
        Kanban kanban = Kanban.builder()
                .team(team)
                .workspace(workspace)
                .build();
        kanban = kanbanRepository.save(kanban);
        
        // 기본 칸반 리스트 생성 (To Do, In Progress, Done)
        KanbanList todoList = createKanbanList(kanban, "To Do", 0);
        KanbanList inProgressList = createKanbanList(kanban, "In Progress", 1);
        KanbanList doneList = createKanbanList(kanban, "Done", 2);
        
        // 샘플 태스크 생성
        if (team.getName().equals("프론트엔드팀")) {
            createFrontendSampleTasks(todoList, inProgressList, doneList, members);
        } else if (team.getName().equals("백엔드팀")) {
            createBackendSampleTasks(todoList, inProgressList, doneList, members);
        }
    }
    
    private KanbanList createKanbanList(Kanban kanban, String name, int order) {
        KanbanList kanbanList = KanbanList.builder()
                .kanbanListName(name)
                .kanban(kanban)
                .order(order)
                .build();
        return kanbanListRepository.save(kanbanList);
    }
    
    private void createFrontendSampleTasks(KanbanList todoList, KanbanList inProgressList, KanbanList doneList, Account... members) {
        // To Do 태스크들
        KanbanTask task1 = createKanbanTask(todoList, "로그인 페이지 UI 개선", 
                "현재 로그인 페이지의 사용자 경험을 개선하고 반응형 디자인을 적용합니다.", 0);
        assignTaskMembers(task1, members[0]); // 팀장에게 할당
        
        KanbanTask task2 = createKanbanTask(todoList, "대시보드 차트 컴포넌트 개발", 
                "Chart.js를 활용하여 데이터 시각화 차트 컴포넌트를 개발합니다.", 1);
        assignTaskMembers(task2, members[1]); // 두 번째 멤버에게 할당
        
        // In Progress 태스크들
        KanbanTask task3 = createKanbanTask(inProgressList, "팀 스페이스 페이지 개발", 
                "팀 협업을 위한 스페이스 페이지를 React로 개발 중입니다.", 0);
        assignTaskMembers(task3, members[0]);
        
        // Done 태스크들
        KanbanTask task4 = createKanbanTask(doneList, "프로젝트 초기 설정", 
                "Vite + React + TypeScript 프로젝트 초기 설정이 완료되었습니다.", 0);
        task4.setIsApproved(true);
        kanbanTaskRepository.save(task4);
        assignTaskMembers(task4, members[0], members[1]);
    }
    
    private void createBackendSampleTasks(KanbanList todoList, KanbanList inProgressList, KanbanList doneList, Account... members) {
        // To Do 태스크들  
        KanbanTask task1 = createKanbanTask(todoList, "칸반 보드 API 성능 최적화", 
                "칸반 보드 조회 시 N+1 쿼리 문제를 해결하고 페이징을 적용합니다.", 0);
        assignTaskMembers(task1, members[1]); // 팀장에게 할당
        
        KanbanTask task2 = createKanbanTask(todoList, "파일 업로드 기능 구현", 
                "칸반 태스크에 첨부파일을 업로드할 수 있는 기능을 구현합니다.", 1);
        assignTaskMembers(task2, members[2]); // 세 번째 멤버에게 할당
        
        // In Progress 태스크들
        KanbanTask task3 = createKanbanTask(inProgressList, "사용자 인증 시스템 개발", 
                "JWT 기반 사용자 인증 및 권한 관리 시스템을 개발 중입니다.", 0);
        assignTaskMembers(task3, members[0], members[1]);
        
        // Done 태스크들
        KanbanTask task4 = createKanbanTask(doneList, "데이터베이스 스키마 설계", 
                "사용자, 워크스페이스, 팀, 칸반 관련 테이블 설계가 완료되었습니다.", 0);
        task4.setIsApproved(true);
        kanbanTaskRepository.save(task4);
        assignTaskMembers(task4, members[1]);
        
        KanbanTask task5 = createKanbanTask(doneList, "기본 CRUD API 개발", 
                "팀 생성, 수정, 삭제, 조회 API가 완료되었습니다.", 1);
        task5.setIsApproved(true);
        kanbanTaskRepository.save(task5);
        assignTaskMembers(task5, members[0], members[2]);
    }
    
    private KanbanTask createKanbanTask(KanbanList kanbanList, String subject, String content, int order) {
        KanbanTask task = KanbanTask.builder()
                .subject(subject)
                .content(content)
                .kanbanList(kanbanList)
                .order(order)
                .isApproved(false)
                .deadline(java.time.LocalDateTime.now().plusDays(7)) // 일주일 후 마감
                .build();
        return kanbanTaskRepository.save(task);
    }
    
    private void assignTaskMembers(KanbanTask task, Account... accounts) {
        for (Account account : accounts) {
            KanbanTaskMember member = KanbanTaskMember.builder()
                    .kanbanTask(task)
                    .account(account)
                    .build();
            kanbanTaskMemberRepository.save(member);
        }
    }
    
    private String generateRandomCode() {
        return java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
} 