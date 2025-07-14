package com.pickteam.repository.video;

import com.pickteam.config.TestQueryDslConfig;
import com.pickteam.domain.videochat.VideoChannel;
import com.pickteam.domain.workspace.Workspace;
import com.pickteam.domain.user.Account;
import com.pickteam.repository.VideoChannelRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 비디오 채널 리포지토리 단위 테스트
 * @DataJpaTest를 사용하여 JPA 관련 기능만 테스트
 */
@DataJpaTest
@Import(TestQueryDslConfig.class)
@ActiveProfiles("test")
class VideoChannelRepositoryTest {

    @Autowired
    private VideoChannelRepository videoChannelRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("워크스페이스 ID로 비디오 채널 목록 조회 시 해당 워크스페이스의 채널들만 반환한다")
    void selectChannelsByWorkSpaceId_ValidWorkspaceId_ReturnsChannelsOfWorkspace() {
        // given
        Account account = Account.builder()
                .email("test@example.com")
                .name("테스트사용자")
                .password("password")
                .build();
        entityManager.persistAndFlush(account);

        Workspace workspace1 = Workspace.builder()
                .name("워크스페이스1")
                .account(account)
                .url("invite123")
                .build();
        entityManager.persistAndFlush(workspace1);

        Workspace workspace2 = Workspace.builder()
                .name("워크스페이스2")
                .account(account)
                .url("invite456")
                .build();
        entityManager.persistAndFlush(workspace2);

        VideoChannel channel1 = VideoChannel.builder()
                .name("채널1")
                .workspace(workspace1)
                .build();
        entityManager.persistAndFlush(channel1);

        VideoChannel channel2 = VideoChannel.builder()
                .name("채널2")
                .workspace(workspace1)
                .build();
        entityManager.persistAndFlush(channel2);

        VideoChannel channel3 = VideoChannel.builder()
                .name("채널3")
                .workspace(workspace2)
                .build();
        entityManager.persistAndFlush(channel3);

        // when
        List<VideoChannel> result = videoChannelRepository.selectChannelsByWorkSpaceId(workspace1.getId());

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactlyInAnyOrder("채널1", "채널2");
        assertThat(result).allMatch(channel -> channel.getWorkspace().getId().equals(workspace1.getId()));
    }

    @Test
    @DisplayName("존재하지 않는 워크스페이스 ID로 채널 조회 시 빈 목록을 반환한다")
    void selectChannelsByWorkSpaceId_NonExistentWorkspaceId_ReturnsEmptyList() {
        // given
        Long nonExistentWorkspaceId = 999L;

        // when
        List<VideoChannel> result = videoChannelRepository.selectChannelsByWorkSpaceId(nonExistentWorkspaceId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("워크스페이스에 채널이 없는 경우 빈 목록을 반환한다")
    void selectChannelsByWorkSpaceId_WorkspaceWithNoChannels_ReturnsEmptyList() {
        // given
        Account account = Account.builder()
                .email("test@example.com")
                .name("테스트사용자")
                .password("password")
                .build();
        entityManager.persistAndFlush(account);

        Workspace workspace = Workspace.builder()
                .name("빈워크스페이스")
                .account(account)
                .url("invite789")
                .build();
        entityManager.persistAndFlush(workspace);

        // when
        List<VideoChannel> result = videoChannelRepository.selectChannelsByWorkSpaceId(workspace.getId());

        // then
        assertThat(result).isEmpty();
    }
}
