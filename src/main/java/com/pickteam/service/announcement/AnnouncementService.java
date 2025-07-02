package com.pickteam.service.announcement;

import com.pickteam.domain.announcement.Announcement;
import com.pickteam.domain.team.Team;
import com.pickteam.domain.user.Account;
import com.pickteam.dto.announcement.AnnouncementCreateRequest;
import com.pickteam.dto.announcement.AnnouncementResponse;
import com.pickteam.dto.announcement.AnnouncementUpdateRequest;
import com.pickteam.repository.announcement.AnnouncementRepository;
import com.pickteam.repository.team.TeamRepository;
import com.pickteam.repository.user.AccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 공지사항 서비스
 * 공지사항 CRUD 및 비즈니스 로직 처리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final AccountRepository accountRepository;
    private final TeamRepository teamRepository;

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
     * 워크스페이스의 모든 공지사항 조회
     *
     * @param workspaceId 워크스페이스 ID
     * @return 공지사항 목록 (최신순 정렬)
     * @throws IllegalArgumentException 잘못된 워크스페이스 ID인 경우
     */
    public List<AnnouncementResponse> getAnnouncementsByWorkspace(Long workspaceId) {
        log.info("워크스페이스 공지사항 조회 요청 - 워크스페이스 ID: {}", workspaceId);

        validateWorkspaceId(workspaceId);

        try {
            List<Announcement> announcements = announcementRepository
                    .findByWorkspaceIdAndIsDeletedFalse(workspaceId);

            log.info("워크스페이스 공지사항 조회 완료 - 워크스페이스 ID: {}, 개수: {}",
                    workspaceId, announcements.size());

            return announcements.stream()
                    .map(AnnouncementResponse::from)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("워크스페이스 공지사항 조회 실패 - 워크스페이스 ID: {}, 오류: {}",
                    workspaceId, e.getMessage(), e);
            throw new RuntimeException("공지사항 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 팀별 공지사항 조회
     *
     * @param teamId 팀 ID
     * @return 해당 팀의 공지사항 목록 (최신순 정렬)
     * @throws IllegalArgumentException 잘못된 팀 ID인 경우
     */
    public List<AnnouncementResponse> getAnnouncementsByTeam(Long teamId) {
        log.info("팀 공지사항 조회 요청 - 팀 ID: {}", teamId);

        validateTeamId(teamId);

        try {
            List<Announcement> announcements = announcementRepository
                    .findByTeamIdAndIsDeletedFalse(teamId);

            log.info("팀 공지사항 조회 완료 - 팀 ID: {}, 개수: {}", teamId, announcements.size());

            return announcements.stream()
                    .map(AnnouncementResponse::from)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("팀 공지사항 조회 실패 - 팀 ID: {}, 오류: {}", teamId, e.getMessage(), e);
            throw new RuntimeException("팀 공지사항 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 단일 공지사항 상세 조회
     *
     * @param announcementId 공지사항 ID
     * @return 공지사항 상세 정보
     * @throws EntityNotFoundException 공지사항을 찾을 수 없는 경우
     * @throws IllegalArgumentException 잘못된 공지사항 ID인 경우
     */
    public AnnouncementResponse getAnnouncement(Long announcementId) {
        log.info("공지사항 조회 요청 - ID: {}", announcementId);

        validateAnnouncementId(announcementId);

        try {
            Announcement announcement = findAnnouncementById(announcementId);

            log.info("공지사항 조회 완료 - ID: {}, 제목: {}",
                    announcementId, announcement.getTitle());

            return AnnouncementResponse.from(announcement);

        } catch (Exception e) {
            log.error("공지사항 조회 실패 - ID: {}, 오류: {}", announcementId, e.getMessage(), e);
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
}