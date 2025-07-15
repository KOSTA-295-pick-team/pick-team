package com.pickteam.service;

import com.pickteam.dto.user.UserSummaryResponse;
import com.pickteam.dto.workspace.*;
import com.pickteam.domain.user.Account;
import com.pickteam.domain.workspace.Blacklist;
import com.pickteam.domain.workspace.Workspace;
import com.pickteam.domain.workspace.WorkspaceMember;
import com.pickteam.repository.user.AccountRepository;
import com.pickteam.repository.workspace.BlacklistRepository;
import com.pickteam.repository.workspace.WorkspaceMemberRepository;
import com.pickteam.repository.workspace.WorkspaceRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkspaceService {
    
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final BlacklistRepository blacklistRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${app.upload.dir}")
    private String uploadPath;
    
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
        
        return joinWorkspaceInternal(accountId, workspace, request.getPassword());
    }
    
    /**
     * 워크스페이스 ID로 직접 참여
     */
    @Transactional
    public WorkspaceResponse joinWorkspaceById(Long accountId, Long workspaceId, String password) {
        Workspace workspace = workspaceRepository.findByIdAndIsDeletedFalse(workspaceId)
                .orElseThrow(() -> new RuntimeException("워크스페이스를 찾을 수 없습니다."));
        
        return joinWorkspaceInternal(accountId, workspace, password);
    }
    
    /**
     * 워크스페이스 참여 공통 로직
     */
    private WorkspaceResponse joinWorkspaceInternal(Long accountId, Workspace workspace, String password) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        if (workspace.getIsDeleted()) {
            throw new RuntimeException("삭제된 워크스페이스입니다.");
        }
        
        // 기존 멤버십 확인
        WorkspaceMember existingMember = workspaceMemberRepository.findByWorkspaceIdAndAccountId(workspace.getId(), accountId)
                .orElse(null);
        
        if (existingMember != null) {
            if (existingMember.getStatus() == WorkspaceMember.MemberStatus.ACTIVE) {
                throw new RuntimeException("이미 워크스페이스의 활성 멤버입니다.");
            } else if (existingMember.getStatus() == WorkspaceMember.MemberStatus.BANNED) {
                throw new RuntimeException("차단된 사용자는 워크스페이스에 참여할 수 없습니다.");
            }
            // LEFT 상태인 경우는 재참여 허용 (아래에서 상태 변경)
        }
        
        // 블랙리스트 확인 (추가 보안)
        if (blacklistRepository.existsByWorkspaceIdAndAccountId(workspace.getId(), accountId)) {
            throw new RuntimeException("차단된 사용자는 워크스페이스에 참여할 수 없습니다.");
        }
        
        // 비밀번호 확인
        if (workspace.getPassword() != null) {
            if (password == null || 
                !passwordEncoder.matches(password, workspace.getPassword())) {
                throw new RuntimeException("워크스페이스 비밀번호가 틀렸습니다.");
            }
        }
        
        // 멤버 처리 (신규 추가 또는 재참여)
        WorkspaceMember member;
        if (existingMember != null && existingMember.getStatus() == WorkspaceMember.MemberStatus.LEFT) {
            // 기존 LEFT 멤버를 ACTIVE로 복귀
            existingMember.setStatus(WorkspaceMember.MemberStatus.ACTIVE);
            member = workspaceMemberRepository.save(existingMember);
        } else {
            // 새로운 멤버 추가
            member = WorkspaceMember.builder()
                    .workspace(workspace)
                    .account(account)
                    .role(WorkspaceMember.MemberRole.MEMBER)
                    .status(WorkspaceMember.MemberStatus.ACTIVE)
                    .build();
            workspaceMemberRepository.save(member);
        }
        
        return convertToResponse(workspace);
    }
    
    /**
     * 워크스페이스 목록 조회 (사용자가 속한)
     */
    public List<WorkspaceResponse> getUserWorkspaces(Long accountId) {
        // WorkspaceMember를 통해 사용자가 속한 활성 멤버십 조회
        List<WorkspaceMember> activeMembers = workspaceMemberRepository.findByAccountIdAndStatus(accountId, WorkspaceMember.MemberStatus.ACTIVE);
        
        // 멤버십에서 워크스페이스 추출 (삭제되지 않은 것만)
        List<Workspace> workspaces = activeMembers.stream()
                .map(WorkspaceMember::getWorkspace)
                .filter(workspace -> !workspace.getIsDeleted())
                .collect(Collectors.toList());
        
        return workspaces.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 워크스페이스 상세 조회
     */
    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspace(Long workspaceId, Long accountId) {
        Workspace workspace = workspaceRepository.findByIdAndIsDeletedFalse(workspaceId)
                .orElseThrow(() -> new RuntimeException("워크스페이스를 찾을 수 없습니다."));
        
        // 활성 멤버인지 확인
        if (!workspaceMemberRepository.existsActiveByWorkspaceIdAndAccountId(workspaceId, accountId)) {
            throw new RuntimeException("워크스페이스 접근 권한이 없습니다.");
        }
        
        return convertToResponse(workspace);
    }
    
    /**
     * 워크스페이스 멤버 목록 조회
     */
    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getWorkspaceMembers(Long workspaceId, Long accountId) {
        // 활성 멤버인지 확인
        if (!workspaceMemberRepository.existsActiveByWorkspaceIdAndAccountId(workspaceId, accountId)) {
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
        
        if (member.getStatus() != WorkspaceMember.MemberStatus.ACTIVE ||
            (member.getRole() != WorkspaceMember.MemberRole.OWNER && 
             member.getRole() != WorkspaceMember.MemberRole.ADMIN)) {
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
        
        if (member.getStatus() != WorkspaceMember.MemberStatus.ACTIVE ||
            (member.getRole() != WorkspaceMember.MemberRole.OWNER && 
             member.getRole() != WorkspaceMember.MemberRole.ADMIN)) {
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
        
        if (requestMember.getStatus() != WorkspaceMember.MemberStatus.ACTIVE ||
            (requestMember.getRole() != WorkspaceMember.MemberRole.OWNER &&
             requestMember.getRole() != WorkspaceMember.MemberRole.ADMIN)) {
            throw new RuntimeException("멤버 내보내기 권한이 없습니다.");
        }
        
        // 대상 멤버 찾기
        WorkspaceMember targetMember = workspaceMemberRepository.findByWorkspaceIdAndAccountId(workspaceId, targetAccountId)
                .orElseThrow(() -> new RuntimeException("대상 멤버를 찾을 수 없습니다."));
        
        // 소유자는 내보낼 수 없음
        if (targetMember.getRole() == WorkspaceMember.MemberRole.OWNER) {
            throw new RuntimeException("소유자는 내보낼 수 없습니다.");
        }
        
        // 현재 상태 확인
        if (targetMember.getStatus() == WorkspaceMember.MemberStatus.LEFT) {
            throw new RuntimeException("이미 내보낸 멤버입니다.");
        }
        if (targetMember.getStatus() == WorkspaceMember.MemberStatus.BANNED) {
            throw new RuntimeException("이미 차단된 멤버입니다. 차단을 해제한 후 다시 시도해주세요.");
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
        
        if (requestMember.getStatus() != WorkspaceMember.MemberStatus.ACTIVE ||
            (requestMember.getRole() != WorkspaceMember.MemberRole.OWNER &&
             requestMember.getRole() != WorkspaceMember.MemberRole.ADMIN)) {
            throw new RuntimeException("멤버 차단 권한이 없습니다.");
        }
        
        // 대상 멤버 찾기
        WorkspaceMember targetMember = workspaceMemberRepository.findByWorkspaceIdAndAccountId(workspaceId, targetAccountId)
                .orElseThrow(() -> new RuntimeException("대상 멤버를 찾을 수 없습니다."));
        
        // 소유자는 차단할 수 없음
        if (targetMember.getRole() == WorkspaceMember.MemberRole.OWNER) {
            throw new RuntimeException("소유자는 차단할 수 없습니다.");
        }
        
        // 현재 상태 확인
        if (targetMember.getStatus() == WorkspaceMember.MemberStatus.BANNED) {
            throw new RuntimeException("이미 차단된 멤버입니다.");
        }
        if (targetMember.getStatus() == WorkspaceMember.MemberStatus.LEFT) {
            throw new RuntimeException("이미 워크스페이스를 나간 멤버입니다.");
        }
        
        // 워크스페이스와 대상 계정 조회
        Workspace workspace = workspaceRepository.findByIdAndIsDeletedFalse(workspaceId)
                .orElseThrow(() -> new RuntimeException("워크스페이스를 찾을 수 없습니다."));
        
        Account targetAccount = accountRepository.findById(targetAccountId)
                .orElseThrow(() -> new RuntimeException("대상 사용자를 찾을 수 없습니다."));
        
        // 멤버 상태 변경
        targetMember.setStatus(WorkspaceMember.MemberStatus.BANNED);
        workspaceMemberRepository.save(targetMember);
        
        // 블랙리스트에 추가 (이미 있으면 무시)
        if (!blacklistRepository.existsByWorkspaceIdAndAccountId(workspaceId, targetAccountId)) {
            Blacklist blacklist = Blacklist.builder()
                    .workspace(workspace)
                    .account(targetAccount)
                    .build();
            blacklistRepository.save(blacklist);
        }
    }
    
    /**
     * 멤버 차단 해제
     */
    @Transactional
    public void unbanMember(Long workspaceId, Long targetAccountId, Long requestAccountId) {
        // 권한 확인
        WorkspaceMember requestMember = workspaceMemberRepository.findByWorkspaceIdAndAccountId(workspaceId, requestAccountId)
                .orElseThrow(() -> new RuntimeException("워크스페이스 접근 권한이 없습니다."));
        
        if (requestMember.getStatus() != WorkspaceMember.MemberStatus.ACTIVE ||
            (requestMember.getRole() != WorkspaceMember.MemberRole.OWNER &&
             requestMember.getRole() != WorkspaceMember.MemberRole.ADMIN)) {
            throw new RuntimeException("멤버 차단 해제 권한이 없습니다.");
        }
        
        // 블랙리스트에서 제거
        blacklistRepository.findByWorkspaceIdAndAccountId(workspaceId, targetAccountId)
                .ifPresent(blacklist -> {
                    blacklist.markDeleted(); // soft delete
                    blacklistRepository.save(blacklist);
                });
        
        // 차단된 멤버의 상태를 LEFT로 변경 (재참여 가능하도록)
        workspaceMemberRepository.findByWorkspaceIdAndAccountId(workspaceId, targetAccountId)
                .ifPresent(member -> {
                    if (member.getStatus() == WorkspaceMember.MemberStatus.BANNED) {
                        member.setStatus(WorkspaceMember.MemberStatus.LEFT);
                        workspaceMemberRepository.save(member);
                    }
                });
    }
    
    /**
     * 워크스페이스 블랙리스트 목록 조회
     */
    public List<UserSummaryResponse> getBlacklistedMembers(Long workspaceId, Long accountId) {
        // 권한 확인
        WorkspaceMember requestMember = workspaceMemberRepository.findByWorkspaceIdAndAccountId(workspaceId, accountId)
                .orElseThrow(() -> new RuntimeException("워크스페이스 접근 권한이 없습니다."));
        
        if (requestMember.getStatus() != WorkspaceMember.MemberStatus.ACTIVE ||
            (requestMember.getRole() != WorkspaceMember.MemberRole.OWNER &&
             requestMember.getRole() != WorkspaceMember.MemberRole.ADMIN)) {
            throw new RuntimeException("블랙리스트 조회 권한이 없습니다.");
        }
        
        List<Blacklist> blacklists = blacklistRepository.findByWorkspaceId(workspaceId);
        return blacklists.stream()
                .map(blacklist -> convertToUserSummary(blacklist.getAccount()))
                .collect(Collectors.toList());
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
    
    /**
     * 워크스페이스 아이콘 업로드
     */
    @Transactional
    public String uploadWorkspaceIcon(Long workspaceId, Long accountId, MultipartFile file) {
        // 워크스페이스 존재 확인
        Workspace workspace = workspaceRepository.findByIdAndIsDeletedFalse(workspaceId)
                .orElseThrow(() -> new RuntimeException("워크스페이스를 찾을 수 없습니다."));
        
        // 권한 확인 (워크스페이스 소유자 또는 관리자만 가능)
        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndAccountId(workspaceId, accountId)
                .orElseThrow(() -> new RuntimeException("워크스페이스 멤버가 아닙니다."));
        
        if (member.getRole() != WorkspaceMember.MemberRole.OWNER && 
            member.getRole() != WorkspaceMember.MemberRole.ADMIN) {
            throw new RuntimeException("워크스페이스 아이콘 변경 권한이 없습니다.");
        }
        
        // 파일 검증
        if (file.isEmpty()) {
            throw new RuntimeException("파일이 비어있습니다.");
        }
        
        // 파일 크기 검증 (5MB 제한)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("파일 크기는 5MB 이하여야 합니다.");
        }
        
        // 파일 타입 검증
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("이미지 파일만 업로드 가능합니다.");
        }
        
        try {
            // 업로드 디렉토리 생성
            Path uploadDir = Paths.get(uploadPath, "workspace-icons");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            // 파일명 생성 (UUID + 원본 확장자)
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
            String filename = UUID.randomUUID().toString() + extension;
            
            // 파일 저장
            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            
            // 파일 URL 생성
            String iconUrl = "/uploads/workspace-icons/" + filename;
            
            // 워크스페이스 아이콘 URL 업데이트
            workspace.setIconUrl(iconUrl);
            workspaceRepository.save(workspace);
            
            return iconUrl;
            
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
        }
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
                .inviteCode(workspace.getUrl()) // 하위 호환성을 위해 유지
                .url(workspace.getUrl()) // 초대 링크용 URL 필드
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
                .email(account.getEmail())
                .age(account.getAge())
                .mbti(account.getMbti())
                .disposition(account.getDisposition())
                .introduction(account.getIntroduction())
                .portfolio(account.getPortfolio())
                .preferWorkstyle(account.getPreferWorkstyle())
                .dislikeWorkstyle(account.getDislikeWorkstyle())
                .profileImageUrl(account.getProfileImageUrl())
                .role(member.getRole().toString())
                .build();
    }
    
    private UserSummaryResponse convertToUserSummary(Account account) {
        return UserSummaryResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .email(account.getEmail())
                .age(account.getAge())
                .mbti(account.getMbti())
                .disposition(account.getDisposition())
                .introduction(account.getIntroduction())
                .portfolio(account.getPortfolio())
                .preferWorkstyle(account.getPreferWorkstyle())
                .dislikeWorkstyle(account.getDislikeWorkstyle())
                .profileImageUrl(account.getProfileImageUrl())
                .role(account.getRole().toString())
                .build();
    }
}