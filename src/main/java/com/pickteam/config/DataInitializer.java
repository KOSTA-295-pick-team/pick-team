package com.pickteam.config;

import com.pickteam.domain.user.Account;
import com.pickteam.domain.workspace.Workspace;
import com.pickteam.domain.workspace.WorkspaceMember;
import com.pickteam.domain.team.Team;
import com.pickteam.domain.team.TeamMember;
import com.pickteam.repository.user.AccountRepository;
import com.pickteam.repository.workspace.WorkspaceRepository;
import com.pickteam.repository.workspace.WorkspaceMemberRepository;
import com.pickteam.repository.team.TeamRepository;
import com.pickteam.repository.team.TeamMemberRepository;
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
                
                System.out.println("테스트 팀이 생성되었습니다:");
                System.out.println("- 프론트엔드팀 (test1@example.com 팀장)");
                System.out.println("- 백엔드팀 (test2@example.com 팀장)");
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
    
    private String generateRandomCode() {
        return java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
} 