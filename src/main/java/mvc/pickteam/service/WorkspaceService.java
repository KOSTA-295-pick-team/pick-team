package mvc.pickteam.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import mvc.pickteam.dto.user.UserSummaryResponse;
import mvc.pickteam.dto.workspace.*;
import mvc.pickteam.entity.Account;
import mvc.pickteam.entity.Workspace;
import mvc.pickteam.entity.WorkspaceMember;
import mvc.pickteam.repository.AccountRepository;
import mvc.pickteam.repository.WorkspaceMemberRepository;
import mvc.pickteam.repository.WorkspaceRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkspaceService {
    
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 워크스페이스 생성
     */
    @Transactional
    public WorkspaceResponse createWorkspace(Long accountId, WorkspaceCreateRequest request) {
        Account owner = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        Workspace workspace = Workspace.builder()
                .name(request.getName())
                .iconUrl(request.getIconUrl())
                .owner(owner)
                .password(request.getPassword() != null ? passwordEncoder.encode(request.getPassword()) : null)
                .inviteCode(generateInviteCode())
                .isDeleted(false)
                .build();
        
        workspace = workspaceRepository.save(workspace);
        
        // 생성자를 OWNER로 멤버에 추가
        WorkspaceMember ownerMember = WorkspaceMember.builder()
                .workspace(workspace)
                .account(owner)
                .role(WorkspaceMember.MemberRole.OWNER)
                .status(WorkspaceMember.MemberStatus.ACTIVE)
                .build();
        
        workspaceMemberRepository.save(ownerMember);
        
        return convertToResponse(workspace);
    }
    
    /**
     * 초대 링크로 워크스페이스 참여
     */
    @Transactional
    public WorkspaceResponse joinWorkspace(Long accountId, WorkspaceJoinRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        Workspace workspace = workspaceRepository.findByInviteCode(request.getInviteCode())
                .orElseThrow(() -> new RuntimeException("유효하지 않은 초대 코드입니다."));
        
        if (workspace.getIsDeleted()) {
            throw new RuntimeException("삭제된 워크스페이스입니다.");
        }
        
        // 이미 멤버인지 확인
        if (workspaceMemberRepository.existsByWorkspaceIdAndAccountId(workspace.getId(), accountId)) {
            throw new RuntimeException("이미 워크스페이스의 멤버입니다.");
        }
        
        // 비밀번호 확인
        if (workspace.getPassword() != null) {
            if (request.getPassword() == null || 
                !passwordEncoder.matches(request.getPassword(), workspace.getPassword())) {
                throw new RuntimeException("워크스페이스 비밀번호가 틀렸습니다.");
            }
        }
        
        // 멤버 추가
        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(workspace)
                .account(account)
                .role(WorkspaceMember.MemberRole.MEMBER)
                .status(WorkspaceMember.MemberStatus.ACTIVE)
                .build();
        
        workspaceMemberRepository.save(member);
        
        return convertToResponse(workspace);
    }
    
    /**
     * 워크스페이스 목록 조회 (사용자가 속한)
     */
    public List<WorkspaceResponse> getUserWorkspaces(Long accountId) {
        List<Workspace> workspaces = workspaceRepository.findByAccountId(accountId);
        return workspaces.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 워크스페이스 상세 조회
     */
    public WorkspaceResponse getWorkspace(Long workspaceId, Long accountId) {
        Workspace workspace = workspaceRepository.findByIdAndIsDeletedFalse(workspaceId)
                .orElseThrow(() -> new RuntimeException("워크스페이스를 찾을 수 없습니다."));
        
        // 멤버인지 확인
        if (!workspaceMemberRepository.existsByWorkspaceIdAndAccountId(workspaceId, accountId)) {
            throw new RuntimeException("워크스페이스 접근 권한이 없습니다.");
        }
        
        return convertToResponse(workspace);
    }
    
    /**
     * 워크스페이스 멤버 목록 조회
     */
    public List<UserSummaryResponse> getWorkspaceMembers(Long workspaceId, Long accountId) {
        // 멤버인지 확인
        if (!workspaceMemberRepository.existsByWorkspaceIdAndAccountId(workspaceId, accountId)) {
            throw new RuntimeException("워크스페이스 접근 권한이 없습니다.");
        }
        
        List<WorkspaceMember> members = workspaceMemberRepository.findActiveMembers(workspaceId);
        return members.stream()
                .map(this::convertToUserSummary)
                .collect(Collectors.toList());
    }
    
    /**
     * 워크스페이스 설정 업데이트
     */
    @Transactional
    public WorkspaceResponse updateWorkspace(Long workspaceId, Long accountId, WorkspaceUpdateRequest request) {
        Workspace workspace = workspaceRepository.findByIdAndIsDeletedFalse(workspaceId)
                .orElseThrow(() -> new RuntimeException("워크스페이스를 찾을 수 없습니다."));
        
        // 소유자 또는 관리자 권한 확인
        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndAccountId(workspaceId, accountId)
                .orElseThrow(() -> new RuntimeException("워크스페이스 접근 권한이 없습니다."));
        
        if (member.getRole() != WorkspaceMember.MemberRole.OWNER && 
            member.getRole() != WorkspaceMember.MemberRole.ADMIN) {
            throw new RuntimeException("워크스페이스 수정 권한이 없습니다.");
        }
        
        // 업데이트
        if (request.getName() != null) {
            workspace.setName(request.getName());
        }
        if (request.getIconUrl() != null) {
            workspace.setIconUrl(request.getIconUrl());
        }
        if (request.getPassword() != null) {
            workspace.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        workspace = workspaceRepository.save(workspace);
        return convertToResponse(workspace);
    }
    
    /**
     * 새 초대 링크 생성
     */
    @Transactional
    public String generateNewInviteCode(Long workspaceId, Long accountId) {
        Workspace workspace = workspaceRepository.findByIdAndIsDeletedFalse(workspaceId)
                .orElseThrow(() -> new RuntimeException("워크스페이스를 찾을 수 없습니다."));
        
        // 소유자 또는 관리자 권한 확인
        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndAccountId(workspaceId, accountId)
                .orElseThrow(() -> new RuntimeException("워크스페이스 접근 권한이 없습니다."));
        
        if (member.getRole() != WorkspaceMember.MemberRole.OWNER && 
            member.getRole() != WorkspaceMember.MemberRole.ADMIN) {
            throw new RuntimeException("초대 링크 생성 권한이 없습니다.");
        }
        
        String newInviteCode = generateInviteCode();
        workspace.setInviteCode(newInviteCode);
        workspaceRepository.save(workspace);
        
        return newInviteCode;
    }
    
    /**
     * 멤버 내보내기
     */
    @Transactional
    public void kickMember(Long workspaceId, Long targetAccountId, Long requestAccountId) {
        // 요청자 권한 확인
        WorkspaceMember requester = workspaceMemberRepository.findByWorkspaceIdAndAccountId(workspaceId, requestAccountId)
                .orElseThrow(() -> new RuntimeException("워크스페이스 접근 권한이 없습니다."));
        
        if (requester.getRole() != WorkspaceMember.MemberRole.OWNER && 
            requester.getRole() != WorkspaceMember.MemberRole.ADMIN) {
            throw new RuntimeException("멤버 내보내기 권한이 없습니다.");
        }
        
        // 대상 멤버 확인
        WorkspaceMember targetMember = workspaceMemberRepository.findByWorkspaceIdAndAccountId(workspaceId, targetAccountId)
                .orElseThrow(() -> new RuntimeException("대상 사용자가 워크스페이스 멤버가 아닙니다."));
        
        // 소유자는 내보낼 수 없음
        if (targetMember.getRole() == WorkspaceMember.MemberRole.OWNER) {
            throw new RuntimeException("워크스페이스 소유자는 내보낼 수 없습니다.");
        }
        
        // 멤버 상태를 LEFT로 변경
        targetMember.setStatus(WorkspaceMember.MemberStatus.LEFT);
        workspaceMemberRepository.save(targetMember);
    }
    
    /**
     * 멤버 영구 밴
     */
    @Transactional
    public void banMember(Long workspaceId, Long targetAccountId, Long requestAccountId) {
        // 요청자 권한 확인
        WorkspaceMember requester = workspaceMemberRepository.findByWorkspaceIdAndAccountId(workspaceId, requestAccountId)
                .orElseThrow(() -> new RuntimeException("워크스페이스 접근 권한이 없습니다."));
        
        if (requester.getRole() != WorkspaceMember.MemberRole.OWNER && 
            requester.getRole() != WorkspaceMember.MemberRole.ADMIN) {
            throw new RuntimeException("멤버 밴 권한이 없습니다.");
        }
        
        // 대상 멤버 확인
        WorkspaceMember targetMember = workspaceMemberRepository.findByWorkspaceIdAndAccountId(workspaceId, targetAccountId)
                .orElseThrow(() -> new RuntimeException("대상 사용자가 워크스페이스 멤버가 아닙니다."));
        
        // 소유자는 밴할 수 없음
        if (targetMember.getRole() == WorkspaceMember.MemberRole.OWNER) {
            throw new RuntimeException("워크스페이스 소유자는 밴할 수 없습니다.");
        }
        
        // 멤버 상태를 BANNED로 변경
        targetMember.setStatus(WorkspaceMember.MemberStatus.BANNED);
        workspaceMemberRepository.save(targetMember);
    }
    
    /**
     * 워크스페이스 삭제
     */
    @Transactional
    public void deleteWorkspace(Long workspaceId, Long accountId) {
        Workspace workspace = workspaceRepository.findByIdAndIsDeletedFalse(workspaceId)
                .orElseThrow(() -> new RuntimeException("워크스페이스를 찾을 수 없습니다."));
        
        // 소유자 권한 확인
        if (!workspace.getOwner().getId().equals(accountId)) {
            throw new RuntimeException("워크스페이스 삭제 권한이 없습니다. 소유자만 삭제할 수 있습니다.");
        }
        
        // 소프트 삭제
        workspace.setIsDeleted(true);
        workspaceRepository.save(workspace);
    }
    
    // Helper methods
    private String generateInviteCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
    
    private WorkspaceResponse convertToResponse(Workspace workspace) {
        List<UserSummaryResponse> members = workspaceMemberRepository.findActiveMembers(workspace.getId())
                .stream()
                .map(this::convertToUserSummary)
                .collect(Collectors.toList());
        
        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .iconUrl(workspace.getIconUrl())
                .ownerId(workspace.getOwner().getId())
                .inviteCode(workspace.getInviteCode())
                .hasPassword(workspace.getPassword() != null)
                .createdAt(workspace.getCreatedAt())
                .members(members)
                .build();
    }
    
    private UserSummaryResponse convertToUserSummary(WorkspaceMember member) {
        return UserSummaryResponse.builder()
                .id(member.getAccount().getId())
                .name(member.getAccount().getName())
                .profileImage(member.getAccount().getProfileImage())
                .role(member.getRole().name())
                .build();
    }
} 