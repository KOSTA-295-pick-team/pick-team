package com.pickteam.service.announcement;

import com.pickteam.domain.announcement.Announcement;
import com.pickteam.domain.team.Team;
import com.pickteam.domain.user.Account;
import com.pickteam.domain.workspace.Workspace;
import com.pickteam.dto.announcement.AnnouncementCreateRequest;
import com.pickteam.dto.announcement.AnnouncementResponse;
import com.pickteam.dto.announcement.AnnouncementUpdateRequest;
import com.pickteam.repository.announcement.AnnouncementRepository;
import com.pickteam.repository.team.TeamRepository;
import com.pickteam.repository.user.AccountRepository;
import com.pickteam.repository.workspace.WorkspaceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * 공지사항 서비스 단위 테스트
 * Business Layer 로직 검증에 집중
 */
@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTest {

    @InjectMocks
    private AnnouncementService announcementService;

    @Mock
    private AnnouncementRepository announcementRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Test
    @DisplayName("공지사항 생성 시 유효한 요청이면 공지사항을 성공적으로 생성한다")
    void createAnnouncement_ValidRequest_ReturnsAnnouncementResponse() {
        // given
        Long accountId = 100L;
        Long teamId = 10L;
        
        AnnouncementCreateRequest request = AnnouncementCreateRequest.builder()
                .title("새로운 공지사항")
                .content("공지사항 내용입니다.")
                .teamId(teamId)
                .build();

        Account mockAccount = Account.builder()
                .id(accountId)
                .email("test@example.com")
                .name("홍길동")
                .build();

        Team mockTeam = Team.builder()
                .id(teamId)
                .name("개발팀")
                .build();

        Announcement mockAnnouncement = Announcement.builder()
                .id(1L)
                .title(request.getTitle())
                .content(request.getContent())
                .account(mockAccount)
                .team(mockTeam)
                .build();

        // Mock 설정
        given(accountRepository.findById(accountId)).willReturn(Optional.of(mockAccount));
        given(teamRepository.findById(teamId)).willReturn(Optional.of(mockTeam));
        given(announcementRepository.save(any(Announcement.class))).willReturn(mockAnnouncement);

        // when
        AnnouncementResponse result = announcementService.createAnnouncement(request, accountId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("새로운 공지사항");
        assertThat(result.getContent()).isEqualTo("공지사항 내용입니다.");
        assertThat(result.getAccountId()).isEqualTo(accountId);
        assertThat(result.getTeamId()).isEqualTo(teamId);

        verify(accountRepository).findById(accountId);
        verify(teamRepository).findById(teamId);
        verify(announcementRepository).save(any(Announcement.class));
    }

    @Test
    @DisplayName("공지사항 생성 시 존재하지 않는 계정이면 예외를 발생시킨다")
    void createAnnouncement_AccountNotFound_ThrowsEntityNotFoundException() {
        // given
        Long invalidAccountId = 999L;
        AnnouncementCreateRequest request = AnnouncementCreateRequest.builder()
                .title("공지사항 제목")
                .content("공지사항 내용")
                .teamId(10L)
                .build();

        given(accountRepository.findById(invalidAccountId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> announcementService.createAnnouncement(request, invalidAccountId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("계정을 찾을 수 없습니다");

        verify(accountRepository).findById(invalidAccountId);
    }

    @Test
    @DisplayName("공지사항 생성 시 존재하지 않는 팀이면 예외를 발생시킨다")
    void createAnnouncement_TeamNotFound_ThrowsEntityNotFoundException() {
        // given
        Long accountId = 100L;
        Long invalidTeamId = 999L;
        
        AnnouncementCreateRequest request = AnnouncementCreateRequest.builder()
                .title("공지사항 제목")
                .content("공지사항 내용")
                .teamId(invalidTeamId)
                .build();

        Account mockAccount = Account.builder()
                .id(accountId)
                .email("test@example.com")
                .name("홍길동")
                .build();

        given(accountRepository.findById(accountId)).willReturn(Optional.of(mockAccount));
        given(teamRepository.findById(invalidTeamId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> announcementService.createAnnouncement(request, accountId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("팀을 찾을 수 없습니다");

        verify(accountRepository).findById(accountId);
        verify(teamRepository).findById(invalidTeamId);
    }

    @Test
    @DisplayName("공지사항 조회 시 유효한 ID이면 공지사항 상세 정보를 반환한다")
    void getAnnouncement_ValidId_ReturnsAnnouncementResponse() {
        // given
        Long workspaceId = 1L;
        Long announcementId = 1L;
        LocalDateTime now = LocalDateTime.now();

        Account mockAccount = Account.builder()
                .id(100L)
                .name("홍길동")
                .email("hong@pickteam.com")
                .build();

        Workspace mockWorkspace = Workspace.builder()
                .id(workspaceId)
                .name("워크스페이스 이름")
                .build();

        Team mockTeam = Team.builder()
                .id(10L)
                .name("개발팀")
                .workspace(mockWorkspace)   // 연관관계 설정
                .build();

        Announcement mockAnnouncement = Announcement.builder()
                .id(announcementId)
                .title("공지사항 제목")
                .content("공지사항 내용")
                .account(mockAccount)
                .team(mockTeam)
                .build();

        given(announcementRepository.findByIdAndIsDeletedFalse(announcementId))
                .willReturn(Optional.of(mockAnnouncement));

        // when
        AnnouncementResponse result = announcementService.getAnnouncement(workspaceId, announcementId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(announcementId);
        assertThat(result.getTitle()).isEqualTo("공지사항 제목");
        assertThat(result.getContent()).isEqualTo("공지사항 내용");
        assertThat(result.getAccountName()).isEqualTo("홍길동");
        assertThat(result.getTeamName()).isEqualTo("개발팀");

        verify(announcementRepository).findByIdAndIsDeletedFalse(announcementId);
    }

    @Test
    @DisplayName("공지사항 조회 시 존재하지 않는 ID이면 예외를 발생시킨다")
    void getAnnouncement_InvalidId_ThrowsEntityNotFoundException() {
        // given
        Long workspaceId = 1L;
        Long invalidAnnouncementId = 999L;

        given(announcementRepository.findByIdAndIsDeletedFalse(invalidAnnouncementId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> announcementService.getAnnouncement(workspaceId, invalidAnnouncementId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("공지사항을 찾을 수 없습니다");

        verify(announcementRepository).findByIdAndIsDeletedFalse(invalidAnnouncementId);
    }

    @Test
    @DisplayName("공지사항 조회 시 워크스페이스 ID가 0 이하이면 예외를 발생시킨다")
    void getAnnouncement_InvalidWorkspaceId_ThrowsIllegalArgumentException() {
        // given
        Long invalidWorkspaceId = 0L;
        Long announcementId = 1L;

        // when & then
        assertThatThrownBy(() -> announcementService.getAnnouncement(invalidWorkspaceId, announcementId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효한 워크스페이스 ID가 필요합니다.");
    }

    @Test
    @DisplayName("공지사항 조회 시 공지사항 ID가 0 이하이면 예외를 발생시킨다")
    void getAnnouncement_InvalidAnnouncementId_ThrowsIllegalArgumentException() {
        // given
        Long workspaceId = 1L;
        Long invalidAnnouncementId = 0L;

        // when & then
        assertThatThrownBy(() -> announcementService.getAnnouncement(workspaceId, invalidAnnouncementId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효한 공지사항 ID가 필요합니다.");
    }
}
