package com.pickteam.service.workspace;

import com.pickteam.domain.user.Account;
import com.pickteam.domain.workspace.Workspace;
import com.pickteam.domain.workspace.WorkspaceMember;
import com.pickteam.dto.user.UserSummaryResponse;
import com.pickteam.dto.workspace.*;
import com.pickteam.repository.user.AccountRepository;
import com.pickteam.repository.workspace.BlacklistRepository;
import com.pickteam.repository.workspace.WorkspaceMemberRepository;
import com.pickteam.repository.workspace.WorkspaceRepository;
import com.pickteam.service.WorkspaceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

/**
 * 워크스페이스 서비스 단위 테스트
 * @ExtendWith(MockitoExtension.class)를 사용하여 Mockito 기반 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class WorkspaceServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Mock
    private BlacklistRepository blacklistRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private WorkspaceService workspaceService;

    @Test
    @DisplayName("워크스페이스 생성 시 정상적으로 생성된다")
    void createWorkspace_ValidRequest_CreatesWorkspace() {
        // given
        Long accountId = 1L;
        WorkspaceCreateRequest request = new WorkspaceCreateRequest();
        request.setName("새로운 워크스페이스");
        request.setIconUrl("🏢");
        request.setPassword("password123");

        Account account = Account.builder()
                .id(accountId)
                .email("test@example.com")
                .name("테스트사용자")
                .build();

        Workspace savedWorkspace = Workspace.builder()
                .id(1L)
                .name("새로운 워크스페이스")
                .iconUrl("🏢")
                .account(account)
                .url("ABC123")
                .password("encodedPassword")
                .build();

        given(accountRepository.findById(accountId))
                .willReturn(Optional.of(account));
        given(passwordEncoder.encode("password123"))
                .willReturn("encodedPassword");
        given(workspaceRepository.save(any(Workspace.class)))
                .willReturn(savedWorkspace);
        given(workspaceMemberRepository.save(any(WorkspaceMember.class)))
                .willReturn(WorkspaceMember.builder().build());
        given(workspaceMemberRepository.findActiveMembers(1L))
                .willReturn(List.of());

        // when
        WorkspaceResponse result = workspaceService.createWorkspace(accountId, request);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("새로운 워크스페이스");
        assertThat(result.getIconUrl()).isEqualTo("🏢");
        assertThat(result.isPasswordProtected()).isTrue();

        verify(accountRepository).findById(accountId);
        verify(passwordEncoder).encode("password123");
        verify(workspaceRepository).save(any(Workspace.class));
        verify(workspaceMemberRepository).save(any(WorkspaceMember.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 워크스페이스 생성 시 예외가 발생한다")
    void createWorkspace_NonExistentUser_ThrowsException() {
        // given
        Long nonExistentAccountId = 999L;
        WorkspaceCreateRequest request = new WorkspaceCreateRequest();
        request.setName("새로운 워크스페이스");

        given(accountRepository.findById(nonExistentAccountId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> workspaceService.createWorkspace(nonExistentAccountId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");

        verify(accountRepository).findById(nonExistentAccountId);
    }

    @Test
    @DisplayName("초대 코드로 워크스페이스 참여 시 정상적으로 참여된다")
    void joinWorkspace_ValidInviteCode_JoinsWorkspace() {
        // given
        Long accountId = 1L;
        WorkspaceJoinRequest request = new WorkspaceJoinRequest();
        request.setInviteCode("ABC123");
        request.setPassword("password123");

        Account account = Account.builder()
                .id(accountId)
                .email("test@example.com")
                .name("테스트사용자")
                .build();

        Workspace workspace = Workspace.builder()
                .id(1L)
                .name("참여할 워크스페이스")
                .url("ABC123")
                .password("encodedPassword")
                .account(account) // workspace owner 설정
                .build();

        given(accountRepository.findById(accountId))
                .willReturn(Optional.of(account));
        given(workspaceRepository.findByUrl("ABC123"))
                .willReturn(Optional.of(workspace));
        given(passwordEncoder.matches("password123", "encodedPassword"))
                .willReturn(true);
        given(workspaceMemberRepository.findByWorkspaceIdAndAccountId(workspace.getId(), account.getId()))
                .willReturn(Optional.empty());
        given(blacklistRepository.existsByWorkspaceIdAndAccountId(workspace.getId(), accountId))
                .willReturn(false);
        given(workspaceMemberRepository.save(any(WorkspaceMember.class)))
                .willReturn(WorkspaceMember.builder().build());
        given(workspaceMemberRepository.findActiveMembers(1L))
                .willReturn(List.of());

        // when
        WorkspaceResponse result = workspaceService.joinWorkspace(accountId, request);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("참여할 워크스페이스");

        verify(accountRepository, times(2)).findById(accountId); // joinWorkspace + joinWorkspaceInternal에서 각각 호출
        verify(workspaceRepository).findByUrl("ABC123");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(workspaceMemberRepository).save(any(WorkspaceMember.class));
    }

    @Test
    @DisplayName("잘못된 초대 코드로 워크스페이스 참여 시 예외가 발생한다")
    void joinWorkspace_InvalidInviteCode_ThrowsException() {
        // given
        Long accountId = 1L;
        WorkspaceJoinRequest request = new WorkspaceJoinRequest();
        request.setInviteCode("INVALID");

        Account account = Account.builder()
                .id(accountId)
                .email("test@example.com")
                .name("테스트사용자")
                .build();

        given(accountRepository.findById(accountId))
                .willReturn(Optional.of(account));
        given(workspaceRepository.findByUrl("INVALID"))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> workspaceService.joinWorkspace(accountId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("유효하지 않은 초대 코드입니다");

        verify(accountRepository).findById(accountId);
        verify(workspaceRepository).findByUrl("INVALID");
    }

    @Test
    @DisplayName("사용자가 속한 워크스페이스 목록 조회 시 정상적으로 반환한다")
    void getUserWorkspaces_ValidAccountId_ReturnsWorkspaceList() {
        // given
        Long accountId = 1L;

        Account account = Account.builder()
                .id(accountId)
                .email("test@example.com")
                .name("테스트사용자")
                .build();

        Workspace workspace1 = Workspace.builder()
                .id(1L)
                .name("워크스페이스 1")
                .iconUrl("🏢")
                .account(account)
                .build();

        Workspace workspace2 = Workspace.builder()
                .id(2L)
                .name("워크스페이스 2")
                .iconUrl("🌟")
                .account(account)
                .build();

        WorkspaceMember member1 = WorkspaceMember.builder()
                .workspace(workspace1)
                .account(account)
                .status(WorkspaceMember.MemberStatus.ACTIVE)
                .build();

        WorkspaceMember member2 = WorkspaceMember.builder()
                .workspace(workspace2)
                .account(account)
                .status(WorkspaceMember.MemberStatus.ACTIVE)
                .build();

        List<WorkspaceMember> memberships = List.of(member1, member2);

        given(workspaceMemberRepository.findByAccountIdAndStatus(accountId, WorkspaceMember.MemberStatus.ACTIVE))
                .willReturn(memberships);
        given(workspaceMemberRepository.findActiveMembers(1L))
                .willReturn(List.of());
        given(workspaceMemberRepository.findActiveMembers(2L))
                .willReturn(List.of());

        // when
        List<WorkspaceResponse> result = workspaceService.getUserWorkspaces(accountId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactly("워크스페이스 1", "워크스페이스 2");
        assertThat(result).extracting("iconUrl").containsExactly("🏢", "🌟");

        verify(workspaceMemberRepository).findByAccountIdAndStatus(accountId, WorkspaceMember.MemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("워크스페이스 상세 조회 시 정상적으로 반환한다")
    void getWorkspace_ValidIds_ReturnsWorkspaceDetails() {
        // given
        Long workspaceId = 1L;
        Long accountId = 1L;

        Account account = Account.builder()
                .id(accountId)
                .email("test@example.com")
                .name("테스트사용자")
                .build();

        Workspace workspace = Workspace.builder()
                .id(workspaceId)
                .name("상세 워크스페이스")
                .iconUrl("🏢")
                .account(account)
                .build();

        given(workspaceRepository.findByIdAndIsDeletedFalse(workspaceId))
                .willReturn(Optional.of(workspace));
        given(workspaceMemberRepository.existsActiveByWorkspaceIdAndAccountId(workspaceId, accountId))
                .willReturn(true);
        given(workspaceMemberRepository.findActiveMembers(workspaceId))
                .willReturn(List.of());

        // when
        WorkspaceResponse result = workspaceService.getWorkspace(workspaceId, accountId);

        // then
        assertThat(result.getId()).isEqualTo(workspaceId);
        assertThat(result.getName()).isEqualTo("상세 워크스페이스");
        assertThat(result.getIconUrl()).isEqualTo("🏢");

        verify(workspaceRepository).findByIdAndIsDeletedFalse(workspaceId);
        verify(workspaceMemberRepository).existsActiveByWorkspaceIdAndAccountId(workspaceId, accountId);
    }

    @Test
    @DisplayName("워크스페이스 멤버 목록 조회 시 정상적으로 반환한다")
    void getWorkspaceMembers_ValidIds_ReturnsMemberList() {
        // given
        Long workspaceId = 1L;
        Long accountId = 1L;

        Account member1 = Account.builder()
                .id(2L)
                .email("member1@example.com")
                .name("멤버1")
                .build();

        Account member2 = Account.builder()
                .id(3L)
                .email("member2@example.com")
                .name("멤버2")
                .build();

        Workspace workspace = Workspace.builder()
                .id(workspaceId)
                .name("테스트 워크스페이스")
                .build();

        WorkspaceMember membership1 = WorkspaceMember.builder()
                .account(member1)
                .workspace(workspace)
                .role(WorkspaceMember.MemberRole.MEMBER)
                .build();

        WorkspaceMember membership2 = WorkspaceMember.builder()
                .account(member2)
                .workspace(workspace)
                .role(WorkspaceMember.MemberRole.MEMBER)
                .build();

        List<WorkspaceMember> members = List.of(membership1, membership2);

        given(workspaceMemberRepository.existsActiveByWorkspaceIdAndAccountId(workspaceId, accountId))
                .willReturn(true);
        given(workspaceMemberRepository.findActiveMembers(workspaceId))
                .willReturn(members);

        // when
        List<UserSummaryResponse> result = workspaceService.getWorkspaceMembers(workspaceId, accountId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactly("멤버1", "멤버2");
        assertThat(result).extracting("email").containsExactly("member1@example.com", "member2@example.com");

        verify(workspaceMemberRepository).existsActiveByWorkspaceIdAndAccountId(workspaceId, accountId);
        verify(workspaceMemberRepository).findActiveMembers(workspaceId);
    }
}
