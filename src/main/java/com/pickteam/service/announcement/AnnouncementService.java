package com.pickteam.service.announcement;

import com.pickteam.domain.announcement.Announcement;
import com.pickteam.domain.team.Team;
import com.pickteam.domain.user.Account;
import com.pickteam.dto.announcement.AnnouncementCreateRequest;
import com.pickteam.dto.announcement.AnnouncementPageResponse;
import com.pickteam.dto.announcement.AnnouncementResponse;
import com.pickteam.dto.announcement.AnnouncementUpdateRequest;
import com.pickteam.repository.announcement.AnnouncementRepository;
import com.pickteam.repository.team.TeamRepository;
import com.pickteam.repository.user.AccountRepository;
import com.pickteam.repository.workspace.WorkspaceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 공지사항 서비스
 * 공지사항 CRUD 및 비즈니스 로직 처리
 * 주요 기능:
 * - 공지사항 생성, 조회
 * - 워크스페이스별 공지사항 관리
 * - 팀별 공지사항 필터링
 * - 작성자 권한 검증 및 보안 처리
 * - 존재 여부 검증 강화
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final AccountRepository accountRepository;
    private final TeamRepository teamRepository;
    private final WorkspaceRepository workspaceRepository;

    /**
     * 공지사항 생성
     * @param request 공지사항 생성 요청
     * @param accountId 작성자 계정 ID
     * @return 생성된 공지사항 응답
     */
    @Transactional
    public AnnouncementResponse createAnnouncement(AnnouncementCreateRequest request, Long accountId) {
        log.info("공지사항 생성 요청 - 계정 ID: {}, 팀 ID: {}, 제목: {}", 
                accountId, request.getTeamId(), request.getTitle());

        try {
            // 요청 데이터 유효성 검증
            validateCreateRequest(request);

            // 계정 조회 및 검증
            Account account = findAndValidateAccount(accountId);

            // 팀 조회 및 검증
            Team team = findTeamById(request.getTeamId());

            // 공지사항 엔티티 생성
            Announcement announcement = createAnnouncementEntity(request, account, team);

            // 저장
            Announcement savedAnnouncement = announcementRepository.save(announcement);
            log.info("공지사항 생성 완료 - ID: {}, 제목: {}", 
                    savedAnnouncement.getId(), savedAnnouncement.getTitle());

            return AnnouncementResponse.from(savedAnnouncement);

        } catch (Exception e) {
            log.error("공지사항 생성 실패 - 계정 ID: {}, 팀 ID: {}, 오류: {}", 
                     accountId, request.getTeamId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 단일 공지사항 상세 조회 (워크스페이스 보안 검증 포함)
     *
     * @param workspaceId 워크스페이스 ID (보안 검증용)
     * @param announcementId 공지사항 ID
     * @return 공지사항 상세 정보
     * @throws EntityNotFoundException 공지사항을 찾을 수 없는 경우
     * @throws IllegalArgumentException 잘못된 ID이거나 워크스페이스 불일치인 경우
     */
    public AnnouncementResponse getAnnouncement(Long workspaceId, Long announcementId) {
        log.info("공지사항 조회 요청 - 워크스페이스 ID: {}, 공지사항 ID: {}", workspaceId, announcementId);

        validateWorkspaceId(workspaceId);
        validateAnnouncementId(announcementId);

        try {
            Announcement announcement = findAnnouncementById(announcementId);

            // 공지사항이 해당 워크스페이스에 속하는지 검증
            validateAnnouncementBelongsToWorkspace(announcement, workspaceId);

            log.info("공지사항 조회 완료 - 워크스페이스 ID: {}, 공지사항 ID: {}, 제목: {}",
                    workspaceId, announcementId, announcement.getTitle());

            return AnnouncementResponse.from(announcement);

        } catch (Exception e) {
            log.error("공지사항 조회 실패 - 워크스페이스 ID: {}, 공지사항 ID: {}, 오류: {}",
                    workspaceId, announcementId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 공지사항 수정 (워크스페이스 보안 검증 포함)
     *
     * @param workspaceId 워크스페이스 ID (보안 검증용)
     * @param announcementId 공지사항 ID
     * @param request 수정 요청 데이터
     * @param accountId 수정 요청자 계정 ID
     * @return 수정된 공지사항 정보
     * @throws EntityNotFoundException 공지사항을 찾을 수 없는 경우
     * @throws IllegalArgumentException 권한이 없거나 잘못된 요청인 경우
     */
    @Transactional
    public AnnouncementResponse updateAnnouncement(Long workspaceId,
                                                   Long announcementId,
                                                   AnnouncementUpdateRequest request,
                                                   Long accountId) {
        log.info("공지사항 수정 요청 - 워크스페이스 ID: {}, 공지사항 ID: {}, 계정 ID: {}, 제목: {}",
                workspaceId, announcementId, accountId, request.getTitle());

        try {
            // 기본 유효성 검증
            validateWorkspaceId(workspaceId);
            validateAnnouncementId(announcementId);
            validateUpdateRequest(request);

            // 공지사항 조회
            Announcement announcement = findAnnouncementById(announcementId);

            // 워크스페이스 일치 검증
            validateAnnouncementBelongsToWorkspace(announcement, workspaceId);

            // 권한 확인 (기존 메서드 사용)
            validateUpdatePermission(announcement, accountId);

            // 공지사항 수정 (엔티티의 update 메서드 사용)
            announcement.update(request.getTitle().trim(),
                    request.getContent() != null ? request.getContent().trim() : null);

            log.info("공지사항 수정 완료 - 워크스페이스 ID: {}, 공지사항 ID: {}, 제목: {}",
                    workspaceId, announcementId, announcement.getTitle());

            return AnnouncementResponse.from(announcement);

        } catch (Exception e) {
            log.error("공지사항 수정 실패 - 워크스페이스 ID: {}, 공지사항 ID: {}, 계정 ID: {}, 오류: {}",
                    workspaceId, announcementId, accountId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 공지사항 소프트 삭제 (워크스페이스 보안 검증 포함)
     *
     * @param workspaceId 워크스페이스 ID (보안 검증용)
     * @param announcementId 공지사항 ID
     * @param accountId 삭제 요청자 계정 ID
     * @throws EntityNotFoundException 공지사항을 찾을 수 없는 경우
     * @throws IllegalArgumentException 권한이 없거나 잘못된 요청인 경우
     */
    @Transactional
    public void deleteAnnouncement(Long workspaceId, Long announcementId, Long accountId) {
        log.info("공지사항 삭제 요청 - 워크스페이스 ID: {}, 공지사항 ID: {}, 계정 ID: {}",
                workspaceId, announcementId, accountId);

        try {
            // 기본 유효성 검증
            validateWorkspaceId(workspaceId);
            validateAnnouncementId(announcementId);

            // 공지사항 조회
            Announcement announcement = findAnnouncementById(announcementId);

            // 워크스페이스 일치 검증
            validateAnnouncementBelongsToWorkspace(announcement, workspaceId);

            // 권한 확인 (기존 메서드 사용)
            validateDeletePermission(announcement, accountId);

            // 소프트 삭제 처리 (엔티티의 markDeleted 메서드 사용)
            announcement.markDeleted();

            log.info("공지사항 삭제 완료 - 워크스페이스 ID: {}, 공지사항 ID: {}, 제목: {}",
                    workspaceId, announcementId, announcement.getTitle());

        } catch (Exception e) {
            log.error("공지사항 삭제 실패 - 워크스페이스 ID: {}, 공지사항 ID: {}, 계정 ID: {}, 오류: {}",
                    workspaceId, announcementId, accountId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 워크스페이스의 공지사항 페이징 조회
     *
     * @param workspaceId 워크스페이스 ID
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 공지사항 페이징 응답
     * @throws IllegalArgumentException 잘못된 워크스페이스 ID인 경우
     * @throws EntityNotFoundException 워크스페이스를 찾을 수 없는 경우
     */
    public AnnouncementPageResponse getAnnouncementsByWorkspaceWithPaging(Long workspaceId, int page, int size) {
        log.info("워크스페이스 공지사항 페이징 조회 요청 - 워크스페이스 ID: {}, 페이지: {}, 크기: {}", 
                workspaceId, page, size);

        validateWorkspaceId(workspaceId);
        validateWorkspaceExists(workspaceId);
        validatePagingParams(page, size);

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Announcement> announcementPage = announcementRepository
                    .findByWorkspaceIdAndIsDeletedFalse(workspaceId, pageable);

            Page<AnnouncementResponse> responsePage = announcementPage
                    .map(AnnouncementResponse::from);

            log.info("워크스페이스 공지사항 페이징 조회 완료 - 워크스페이스 ID: {}, 총 개수: {}, 현재 페이지: {}/{}",
                    workspaceId, responsePage.getTotalElements(), page + 1, responsePage.getTotalPages());

            return AnnouncementPageResponse.from(responsePage);

        } catch (Exception e) {
            log.error("워크스페이스 공지사항 페이징 조회 실패 - 워크스페이스 ID: {}, 오류: {}",
                    workspaceId, e.getMessage(), e);
            throw new RuntimeException("공지사항 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 팀별 공지사항 페이징 조회 (워크스페이스 보안 검증 포함)
     *
     * @param workspaceId 워크스페이스 ID (보안 검증용)
     * @param teamId 팀 ID
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 해당 팀의 공지사항 페이징 응답
     * @throws IllegalArgumentException 잘못된 ID이거나 워크스페이스-팀 불일치인 경우
     * @throws EntityNotFoundException 팀을 찾을 수 없는 경우
     */
    public AnnouncementPageResponse getAnnouncementsByTeamWithPaging(Long workspaceId, Long teamId, int page, int size) {
        log.info("팀 공지사항 페이징 조회 요청 - 워크스페이스 ID: {}, 팀 ID: {}, 페이지: {}, 크기: {}", 
                workspaceId, teamId, page, size);

        validateWorkspaceId(workspaceId);
        validateTeamId(teamId);
        validatePagingParams(page, size);

        // 팀 존재 여부 확인 및 워크스페이스 일치 검증
        Team team = findTeamById(teamId);
        validateTeamBelongsToWorkspace(team, workspaceId);

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Announcement> announcementPage = announcementRepository
                    .findByTeamIdAndIsDeletedFalse(teamId, pageable);

            Page<AnnouncementResponse> responsePage = announcementPage
                    .map(AnnouncementResponse::from);

            log.info("팀 공지사항 페이징 조회 완료 - 워크스페이스 ID: {}, 팀 ID: {}, 총 개수: {}, 현재 페이지: {}/{}",
                    workspaceId, teamId, responsePage.getTotalElements(), page + 1, responsePage.getTotalPages());

            return AnnouncementPageResponse.from(responsePage);

        } catch (Exception e) {
            log.error("팀 공지사항 페이징 조회 실패 - 워크스페이스 ID: {}, 팀 ID: {}, 오류: {}",
                    workspaceId, teamId, e.getMessage(), e);
            throw e;
        }
    }

    // === Private 검증 메서드들 ===

    private void validateCreateRequest(AnnouncementCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("요청 데이터가 필요합니다.");
        }
        request.validate();
    }

    private void validateUpdateRequest(AnnouncementUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("요청 데이터가 필요합니다.");
        }
        request.validate();
    }

    private void validateWorkspaceId(Long workspaceId) {
        if (workspaceId == null || workspaceId <= 0) {
            throw new IllegalArgumentException("유효한 워크스페이스 ID가 필요합니다.");
        }
    }

    /**
     * 워크스페이스 존재 여부 검증
     *
     * @param workspaceId 워크스페이스 ID
     * @throws EntityNotFoundException 워크스페이스를 찾을 수 없는 경우
     */
    // 수정 (기존 메서드 사용)
    private void validateWorkspaceExists(Long workspaceId) {
        workspaceRepository.findByIdAndIsDeletedFalse(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("워크스페이스를 찾을 수 없습니다. ID: " + workspaceId));
    }

    /**
     * 팀이 해당 워크스페이스에 속하는지 검증
     *
     * @param team 팀 엔티티
     * @param workspaceId 워크스페이스 ID
     * @throws IllegalArgumentException 팀이 해당 워크스페이스에 속하지 않는 경우
     */
    private void validateTeamBelongsToWorkspace(Team team, Long workspaceId) {
        if (!team.getWorkspace().getId().equals(workspaceId)) {
            throw new IllegalArgumentException(
                    String.format("팀(ID: %d)이 워크스페이스(ID: %d)에 속하지 않습니다.",
                            team.getId(), workspaceId));
        }
    }

    /**
     * 공지사항이 해당 워크스페이스에 속하는지 검증
     *
     * @param announcement 공지사항 엔티티
     * @param workspaceId 워크스페이스 ID
     * @throws IllegalArgumentException 공지사항이 해당 워크스페이스에 속하지 않는 경우
     */
    private void validateAnnouncementBelongsToWorkspace(Announcement announcement, Long workspaceId) {
        if (!announcement.getTeam().getWorkspace().getId().equals(workspaceId)) {
            throw new IllegalArgumentException(
                    String.format("공지사항(ID: %d)이 워크스페이스(ID: %d)에 속하지 않습니다.",
                            announcement.getId(), workspaceId));
        }
    }

    private void validateAnnouncementId(Long announcementId) {
        if (announcementId == null || announcementId <= 0) {
            throw new IllegalArgumentException("유효한 공지사항 ID가 필요합니다.");
        }
    }

    private void validateTeamId(Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new IllegalArgumentException("유효한 팀 ID가 필요합니다.");
        }
    }

    private Account findAndValidateAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("계정을 찾을 수 없습니다. ID: " + accountId));

        if (account.isWithdrawnUser()) {
            throw new IllegalArgumentException("탈퇴한 계정은 공지사항을 작성할 수 없습니다.");
        }

        return account;
    }

    private Team findTeamById(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("팀을 찾을 수 없습니다. ID: " + teamId));
    }

    private Announcement findAnnouncementById(Long announcementId) {
        return announcementRepository.findByIdAndIsDeletedFalse(announcementId)
                .orElseThrow(() -> new EntityNotFoundException("공지사항을 찾을 수 없습니다. ID: " + announcementId));
    }

    private Announcement createAnnouncementEntity(AnnouncementCreateRequest request, Account account, Team team) {
        return Announcement.builder()
                .title(request.getTitle().trim())
                .content(request.getContent() != null ? request.getContent().trim() : null)
                .account(account)
                .team(team)
                .build();
    }

    private void validateUpdatePermission(Announcement announcement, Long accountId) {
        if (!announcement.isAuthor(accountId)) {
            throw new IllegalArgumentException("공지사항을 수정할 권한이 없습니다.");
        }

        if (announcement.isAuthorWithdrawn()) {
            throw new IllegalArgumentException("탈퇴한 계정이 작성한 공지사항은 수정할 수 없습니다.");
        }
    }

    private void validateDeletePermission(Announcement announcement, Long accountId) {
        if (!announcement.isAuthor(accountId)) {
            throw new IllegalArgumentException("공지사항을 삭제할 권한이 없습니다.");
        }
    }

    /**
     * 페이징 파라미터 유효성 검증
     *
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @throws IllegalArgumentException 잘못된 페이징 파라미터인 경우
     */
    private void validatePagingParams(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("페이지 번호는 0 이상이어야 합니다.");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("페이지 크기는 1 이상이어야 합니다.");
        }
        if (size > 50) {
            throw new IllegalArgumentException("페이지 크기는 50 이하여야 합니다.");
        }
    }
}