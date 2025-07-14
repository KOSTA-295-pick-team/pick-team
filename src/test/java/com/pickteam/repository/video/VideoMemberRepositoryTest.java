package com.pickteam.repository.video;

import com.pickteam.config.TestQueryDslConfig;
import com.pickteam.domain.videochat.VideoMember;
import com.pickteam.domain.videochat.VideoChannel;
import com.pickteam.domain.workspace.Workspace;
import com.pickteam.domain.user.Account;
import com.pickteam.repository.VideoMemberRepository;
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
 * 비디오 멤버 리포지토리 단위 테스트
 * @DataJpaTest를 사용하여 JPA 관련 기능만 테스트
 */
@DataJpaTest
@Import(TestQueryDslConfig.class)
@ActiveProfiles("test")
class VideoMemberRepositoryTest {

    @Autowired
    private VideoMemberRepository videoMemberRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("채널 ID로 비디오 멤버 목록 조회 시 해당 채널의 멤버들만 반환한다")
    void selectAccountsByChannelId_ValidChannelId_ReturnsMembersOfChannel() {
        // given
        Account account1 = Account.builder()
                .email("user1@example.com")
                .name("사용자1")
                .password("password")
                .build();
        entityManager.persistAndFlush(account1);

        Account account2 = Account.builder()
                .email("user2@example.com")
                .name("사용자2")
                .password("password")
                .build();
        entityManager.persistAndFlush(account2);

        Account account3 = Account.builder()
                .email("user3@example.com")
                .name("사용자3")
                .password("password")
                .build();
        entityManager.persistAndFlush(account3);

        Workspace workspace = Workspace.builder()
                .name("테스트워크스페이스")
                .account(account1)
                .url("invite123")
                .build();
        entityManager.persistAndFlush(workspace);

        VideoChannel channel1 = VideoChannel.builder()
                .name("채널1")
                .workspace(workspace)
                .build();
        entityManager.persistAndFlush(channel1);

        VideoChannel channel2 = VideoChannel.builder()
                .name("채널2")
                .workspace(workspace)
                .build();
        entityManager.persistAndFlush(channel2);

        VideoMember member1 = VideoMember.builder()
                .account(account1)
                .videoChannel(channel1)
                .build();
        entityManager.persistAndFlush(member1);

        VideoMember member2 = VideoMember.builder()
                .account(account2)
                .videoChannel(channel1)
                .build();
        entityManager.persistAndFlush(member2);

        VideoMember member3 = VideoMember.builder()
                .account(account3)
                .videoChannel(channel2)
                .build();
        entityManager.persistAndFlush(member3);

        // when
        List<VideoMember> result = videoMemberRepository.selectAccountsByChannelId(channel1.getId());

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(member -> member.getAccount().getName())
                .containsExactlyInAnyOrder("사용자1", "사용자2");
        assertThat(result).allMatch(member -> member.getVideoChannel().getId().equals(channel1.getId()));
    }

    @Test
    @DisplayName("존재하지 않는 채널 ID로 멤버 조회 시 빈 목록을 반환한다")
    void selectAccountsByChannelId_NonExistentChannelId_ReturnsEmptyList() {
        // given
        Long nonExistentChannelId = 999L;

        // when
        List<VideoMember> result = videoMemberRepository.selectAccountsByChannelId(nonExistentChannelId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("채널에 멤버가 없는 경우 빈 목록을 반환한다")
    void selectAccountsByChannelId_ChannelWithNoMembers_ReturnsEmptyList() {
        // given
        Account account = Account.builder()
                .email("test@example.com")
                .name("테스트사용자")
                .password("password")
                .build();
        entityManager.persistAndFlush(account);

        Workspace workspace = Workspace.builder()
                .name("테스트워크스페이스")
                .account(account)
                .url("invite123")
                .build();
        entityManager.persistAndFlush(workspace);

        VideoChannel channel = VideoChannel.builder()
                .name("빈채널")
                .workspace(workspace)
                .build();
        entityManager.persistAndFlush(channel);

        // when
        List<VideoMember> result = videoMemberRepository.selectAccountsByChannelId(channel.getId());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("계정 정보가 함께 조회되는지 확인한다")
    void selectAccountsByChannelId_ValidChannelId_ReturnsMembersWithAccountInfo() {
        // given
        Account account = Account.builder()
                .email("test@example.com")
                .name("테스트사용자")
                .password("password")
                .build();
        entityManager.persistAndFlush(account);

        Workspace workspace = Workspace.builder()
                .name("테스트워크스페이스")
                .account(account)
                .url("invite123")
                .build();
        entityManager.persistAndFlush(workspace);

        VideoChannel channel = VideoChannel.builder()
                .name("테스트채널")
                .workspace(workspace)
                .build();
        entityManager.persistAndFlush(channel);

        VideoMember member = VideoMember.builder()
                .account(account)
                .videoChannel(channel)
                .build();
        entityManager.persistAndFlush(member);

        entityManager.clear(); // 영속성 컨텍스트 초기화

        // when
        List<VideoMember> result = videoMemberRepository.selectAccountsByChannelId(channel.getId());

        // then
        assertThat(result).hasSize(1);
        VideoMember foundMember = result.get(0);
        assertThat(foundMember.getAccount()).isNotNull();
        assertThat(foundMember.getAccount().getEmail()).isEqualTo("test@example.com");
        assertThat(foundMember.getAccount().getName()).isEqualTo("테스트사용자");
    }
}
