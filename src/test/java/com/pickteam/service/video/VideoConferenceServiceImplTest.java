package com.pickteam.service.video;

import com.pickteam.domain.user.Account;
import com.pickteam.domain.videochat.VideoChannel;
import com.pickteam.domain.videochat.VideoMember;
import com.pickteam.domain.workspace.Workspace;
import com.pickteam.dto.VideoChannelDTO;
import com.pickteam.dto.VideoMemberDTO;
import com.pickteam.exception.VideoConferenceErrorCode;
import com.pickteam.exception.VideoConferenceException;
import com.pickteam.repository.VideoChannelRepository;
import com.pickteam.repository.VideoMemberRepository;
import com.pickteam.repository.user.AccountRepository;
import com.pickteam.repository.workspace.WorkspaceRepository;
import com.pickteam.service.VideoConferenceServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * 비디오 컨퍼런스 서비스 단위 테스트
 * @ExtendWith(MockitoExtension.class)를 사용하여 Mockito 기반 단위 테스트
 */
/**
@ExtendWith(MockitoExtension.class)
class VideoConferenceServiceImplTest {

    @Mock
    private VideoChannelRepository videoChannelRepository;

    @Mock
    private VideoMemberRepository videoMemberRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @InjectMocks
    private VideoConferenceServiceImpl videoConferenceService;

    @Test
    @DisplayName("워크스페이스의 비디오 채널 목록 조회 시 정상적으로 반환한다 (accountId 있음)")
    void selectVideoChannels_ValidWorkspaceIdWithAccountId_ReturnsChannelList() throws VideoConferenceException {
        // given
        Long workspaceId = 1L;
        Long accountId = 100L;

        VideoChannel channel1 = VideoChannel.builder()
                .id(1L)
                .name("일반 회의실")
                .build();

        VideoChannel channel2 = VideoChannel.builder()
                .id(2L)
                .name("개발팀 회의실")
                .build();

        List<VideoChannel> channels = List.of(channel1, channel2);

        given(videoChannelRepository.selectChannelsByWorkSpaceId(workspaceId, accountId))
                .willReturn(channels);

        // when
        List<VideoChannelDTO> result = videoConferenceService.selectVideoChannels(workspaceId, accountId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactly("일반 회의실", "개발팀 회의실");
        verify(videoChannelRepository).selectChannelsByWorkSpaceId(workspaceId, accountId);
    }

    @Test
    @DisplayName("워크스페이스의 비디오 채널 목록 조회 시 정상적으로 반환한다 (accountId 없음)")
    void selectVideoChannels_ValidWorkspaceIdWithoutAccountId_ReturnsChannelList() throws VideoConferenceException {
        // given
        Long workspaceId = 1L;
        Long accountId = null;

        VideoChannel channel1 = VideoChannel.builder()
                .id(1L)
                .name("일반 회의실")
                .build();

        VideoChannel channel2 = VideoChannel.builder()
                .id(2L)
                .name("개발팀 회의실")
                .build();

        List<VideoChannel> channels = List.of(channel1, channel2);

        given(videoChannelRepository.selectChannelsByWorkSpaceId(workspaceId))
                .willReturn(channels);

        // when
        List<VideoChannelDTO> result = videoConferenceService.selectVideoChannels(workspaceId, accountId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactly("일반 회의실", "개발팀 회의실");
        verify(videoChannelRepository).selectChannelsByWorkSpaceId(workspaceId);
    }

    @Test
    @DisplayName("비디오 채널 생성 시 정상적으로 생성된다")
    void insertVideoChannel_ValidRequest_CreatesChannel() {
        // given
        Long workspaceId = 1L;
        String channelName = "새로운 회의실";

        VideoChannel savedChannel = VideoChannel.builder()
                .id(1L)
                .name(channelName)
                .workspace(Workspace.builder().id(workspaceId).build())
                .build();

        given(videoChannelRepository.save(any(VideoChannel.class)))
                .willReturn(savedChannel);

        // when
        videoConferenceService.insertVideoChannel(workspaceId, channelName);

        // then
        verify(videoChannelRepository).save(any(VideoChannel.class));
    }

    @Test
    @DisplayName("비디오 채널 참가자 목록 조회 시 정상적으로 반환한다")
    void selectVideoChannelParticipants_ValidChannelId_ReturnsParticipantList() throws VideoConferenceException {
        // given
        Long channelId = 1L;

        Account account1 = Account.builder()
                .id(100L)
                .email("user1@example.com")
                .name("사용자1")
                .build();

        Account account2 = Account.builder()
                .id(200L)
                .email("user2@example.com")
                .name("사용자2")
                .build();

        VideoMember member1 = VideoMember.builder()
                .id(1L)
                .account(account1)
                .build();

        VideoMember member2 = VideoMember.builder()
                .id(2L)
                .account(account2)
                .build();

        List<VideoMember> members = List.of(member1, member2);

        given(videoMemberRepository.selectAccountsByChannelId(channelId))
                .willReturn(members);

        // when
        List<VideoMemberDTO> result = videoConferenceService.selectVideoChannelParticipants(channelId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("email").containsExactly("user1@example.com", "user2@example.com");
        assertThat(result).extracting("name").containsExactly("사용자1", "사용자2");
        verify(videoMemberRepository).selectAccountsByChannelId(channelId);
    }

    @Test
    @DisplayName("비디오 채널 삭제 시 정상적으로 삭제된다")
    void deleteVideoChannel_ValidChannelId_DeletesChannel() throws VideoConferenceException {
        // given
        Long channelId = 1L;

        VideoChannel channel = VideoChannel.builder()
                .id(channelId)
                .name("삭제할 채널")
                .build();

        given(videoChannelRepository.findById(channelId))
                .willReturn(Optional.of(channel));

        // when
        videoConferenceService.deleteVideoChannel(channelId);

        // then
        verify(videoChannelRepository).findById(channelId);
        verify(videoChannelRepository).delete(channel);
    }

    @Test
    @DisplayName("존재하지 않는 채널 삭제 시 예외가 발생한다")
    void deleteVideoChannel_NonExistentChannel_ThrowsException() {
        // given
        Long nonExistentChannelId = 999L;

        given(videoChannelRepository.findById(nonExistentChannelId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> videoConferenceService.deleteVideoChannel(nonExistentChannelId))
                .isInstanceOf(VideoConferenceException.class)
                .extracting("videoConferenceErrorCode")
                .isEqualTo(VideoConferenceErrorCode.CHANNEL_NOT_FOUND);

        verify(videoChannelRepository).findById(nonExistentChannelId);
    }

    @Test
    @DisplayName("비디오 채널 참가자 삭제 시 정상적으로 삭제된다")
    void deleteVideoChannelParticipant_ValidMemberId_DeletesParticipant() throws VideoConferenceException {
        // given
        Long memberId = 1L;

        VideoMember member = VideoMember.builder()
                .id(memberId)
                .build();

        given(videoMemberRepository.findById(memberId))
                .willReturn(Optional.of(member));

        // when
        videoConferenceService.deleteVideoChannelParticipant(memberId);

        // then
        verify(videoMemberRepository).findById(memberId);
        verify(videoMemberRepository).delete(member);
    }

    @Test
    @DisplayName("존재하지 않는 참가자 삭제 시 예외가 발생한다")
    void deleteVideoChannelParticipant_NonExistentMember_ThrowsException() {
        // given
        Long nonExistentMemberId = 999L;

        given(videoMemberRepository.findById(nonExistentMemberId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> videoConferenceService.deleteVideoChannelParticipant(nonExistentMemberId))
                .isInstanceOf(VideoConferenceException.class)
                .extracting("videoConferenceErrorCode")
                .isEqualTo(VideoConferenceErrorCode.MEMBER_NOT_FOUND);

        verify(videoMemberRepository).findById(nonExistentMemberId);
    }
}
**/