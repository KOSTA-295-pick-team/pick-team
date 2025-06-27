package com.pickteam.service;

import com.pickteam.dto.user.UserSummaryResponse;
import com.pickteam.dto.workspace.*;
import com.pickteam.domain.user.Account;
import com.pickteam.domain.workspace.Workspace;
import com.pickteam.domain.workspace.WorkspaceMember;
import com.pickteam.repository.user.AccountRepository;
import com.pickteam.repository.workspace.WorkspaceMemberRepository;
import com.pickteam.repository.workspace.WorkspaceRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Random;

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
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        Workspace workspace = Workspace.builder()
                .name(request.getName())
                .iconUrl(request.getIconUrl())
                .account(account)
                .password(request.getPassword() != null ? passwordEncoder.encode(request.getPassword()) : null)
                .url(generateInviteCode())
                .build();
        
        workspace = workspaceRepository.save(workspace);
        
        // 생성자를 OWNER로 멤버에 추가
        WorkspaceMember ownerMember = WorkspaceMember.builder()
                .workspace(workspace)
                .account(account)
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
        
        Workspace workspace = workspaceRepository.findByUrl(request.getInviteCode())
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
        workspace.setUrl(newInviteCode);
        workspaceRepository.save(workspace);
        
        return newInviteCode;
    }
    
    /**
     * 멤버 내보내기
     */
    @Transactional
    public void kickMember(Long workspaceId, Long targetAccountId, Long requestAccountId) {
        // 권한 확인
        WorkspaceMember requestMember = workspaceMemberRepository.findByWorkspaceIdAndAccountId(workspaceId, requestAccountId)
                .orElseThrow(() -> new RuntimeException("워크스페이스 접근 권한이 없습니다."));
        
        if (requestMember.getRole() != WorkspaceMember.MemberRole.OWNER &&
            requestMember.getRole() != WorkspaceMember.MemberRole.ADMIN) {
            throw new RuntimeException("멤버 내보내기 권한이 없습니다.");
        }
        
        // 대상 멤버 찾기
        WorkspaceMember targetMember = workspaceMemberRepository.findByWorkspaceIdAndAccountId(workspaceId, targetAccountId)
                .orElseThrow(() -> new RuntimeException("대상 멤버를 찾을 수 없습니다."));
        
        // 소유자는 내보낼 수 없음
        if (targetMember.getRole() == WorkspaceMember.MemberRole.OWNER) {
            throw new RuntimeException("소유자는 내보낼 수 없습니다.");
        }
        
        // 상태 변경
        targetMember.setStatus(WorkspaceMember.MemberStatus.LEFT);
        workspaceMemberRepository.save(targetMember);
    }
    
    /**
     * 멤버 차단
     */
    @Transactional
    public void banMember(Long workspaceId, Long targetAccountId, Long requestAccountId) {
        // 권한 확인
        WorkspaceMember requestMember = workspaceMemberRepository.findByWorkspaceIdAndAccountId(workspaceId, requestAccountId)
                .orElseThrow(() -> new RuntimeException("워크스페이스 접근 권한이 없습니다."));
        
        if (requestMember.getRole() != WorkspaceMember.MemberRole.OWNER &&
            requestMember.getRole() != WorkspaceMember.MemberRole.ADMIN) {
            throw new RuntimeException("멤버 차단 권한이 없습니다.");
        }
        
        // 대상 멤버 찾기
        WorkspaceMember targetMember = workspaceMemberRepository.findByWorkspaceIdAndAccountId(workspaceId, targetAccountId)
                .orElseThrow(() -> new RuntimeException("대상 멤버를 찾을 수 없습니다."));
        
        // 소유자는 차단할 수 없음
        if (targetMember.getRole() == WorkspaceMember.MemberRole.OWNER) {
            throw new RuntimeException("소유자는 차단할 수 없습니다.");
        }
        
        // 상태 변경
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
        if (!workspace.getAccount().getId().equals(accountId)) {
            throw new RuntimeException("워크스페이스 삭제 권한이 없습니다.");
        }
        
        workspace.markDeleted();
        workspaceRepository.save(workspace);
    }
    
    private String generateInviteCode() {
        // 혼동하기 쉬운 문자 제외: 0(zero), O(oh), 1(one), l(L), I(i)
        final String CHARACTERS = "23456789abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ";
        final Random random = new Random();
        
        String inviteCode;
        int attempts = 0;
        final int MAX_ATTEMPTS = 10;
        
        do {
            StringBuilder sb = new StringBuilder();
            
            // 기본 8자리 코드 생성
            for (int i = 0; i < 8; i++) {
                sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
            }
            
            inviteCode = sb.toString();
            attempts++;
            
            // 최대 시도 횟수 초과 시 더 긴 코드 생성
            if (attempts >= MAX_ATTEMPTS) {
                for (int i = 0; i < 4; i++) { // 4자리 추가 (총 12자리)
                    sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
                }
                inviteCode = sb.toString();
            }
            
        } while (workspaceRepository.findByUrl(inviteCode).isPresent() && attempts < MAX_ATTEMPTS * 2);
        
        // 그래도 중복이면 타임스탬프 추가
        if (workspaceRepository.findByUrl(inviteCode).isPresent()) {
            String timestamp = String.valueOf(System.currentTimeMillis()).substring(8); // 마지막 5자리
            inviteCode = inviteCode.substring(0, Math.min(inviteCode.length(), 6)) + timestamp;
        }
        
        return inviteCode;
    }
    
    private WorkspaceResponse convertToResponse(Workspace workspace) {
        List<WorkspaceMember> members = workspaceMemberRepository.findActiveMembers(workspace.getId());
        
        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .iconUrl(workspace.getIconUrl())
                .owner(convertToUserSummary(workspace.getAccount()))
                .passwordProtected(workspace.getPassword() != null)
                .inviteCode(workspace.getUrl())
                .memberCount(members.size())
                .members(members.stream().map(this::convertToUserSummary).collect(Collectors.toList()))
                .createdAt(workspace.getCreatedAt())
                .updatedAt(workspace.getUpdatedAt())
                .build();
    }
    
    private UserSummaryResponse convertToUserSummary(WorkspaceMember member) {
        Account account = member.getAccount();
        return UserSummaryResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .profileImage(account.getProfileImage())
                .role(member.getRole().toString())
                .build();
    }
    
    private UserSummaryResponse convertToUserSummary(Account account) {
        return UserSummaryResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .profileImage(account.getProfileImage())
                .role(account.getRole().toString())
                .build();
    }
} 