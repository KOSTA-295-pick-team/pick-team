package com;

import com.pickteam.domain.enums.UserRole;
import com.pickteam.domain.user.Account;
import com.pickteam.domain.workspace.Workspace;
import com.pickteam.dto.VideoChannelDTO;
import com.pickteam.exception.VideoConferenceException;
import com.pickteam.repository.VideoChannelRepository;
import com.pickteam.repository.VideoMemberRepository;
import com.pickteam.repository.user.AccountRepository;
import com.pickteam.repository.workspace.WorkspaceRepository;
import com.pickteam.service.VideoConferenceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

/**
@SpringBootTest
class PickTeamApplicationTests {

    @Autowired
    VideoMemberRepository videoMemberRepository;

    @Autowired
    VideoChannelRepository videoChannelRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    VideoConferenceService videoConferenceService;

    @Autowired
    WorkspaceRepository workspaceRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void contextLoads() {
    }

    @Test
    void insertAccountMember() {
        Account admin = Account.builder()
                .email("admin@example.com")
                .password("password123")
                .name("관리자")
                .age(30)
                .role(UserRole.ADMIN)
                .mbti("INTJ")
                .disposition("차분함")
                .introduction("안녕하세요, 관리자입니다.")
                .portfolio("https://github.com/admin")
                .preferWorkstyle("계획적인 업무")
                .dislikeWorkstyle("즉흥적인 업무")
                .build();

        Account user1 = Account.builder()
                .email("user1@example.com")
                .password("password123")
                .name("홍길동")
                .age(25)
                .role(UserRole.USER)
                .mbti("ENFP")
                .disposition("활발함")
                .introduction("안녕하세요, 홍길동입니다.")
                .portfolio("https://github.com/user1")
                .preferWorkstyle("자유로운 분위기")
                .dislikeWorkstyle("경직된 분위기")
                .build();

        Account user2 = Account.builder()
                .email("user2@example.com")
                .password("password123")
                .name("김철수")
                .age(28)
                .role(UserRole.USER)
                .mbti("ISFJ")
                .disposition("성실함")
                .introduction("안녕하세요, 김철수입니다.")
                .portfolio("https://github.com/user2")
                .preferWorkstyle("체계적인 업무")
                .dislikeWorkstyle("무질서한 환경")
                .build();

        admin = accountRepository.save(admin);
        user1 = accountRepository.save(user1);
        user2 = accountRepository.save(user2);
    }

    @Test
    void insertWorkspaces() {
        Workspace workspace = Workspace.builder()
                .name("테스트 워크스페이스")
                .password("workspace123")
                .url("test-workspace")
                .account(Account.builder().id(1L).build())
                .build();

        workspace = workspaceRepository.save(workspace);
    }


    @Test
    @DisplayName("화상회의채널 추가 test")
    void insertVideoChannel() throws VideoConferenceException {
        videoConferenceService.insertVideoChannel(1L, "ㅇㅇ채널");
    }

    @Test
    @DisplayName("화상회의채널 조회 TEST")
    void selectVideoChannel() throws VideoConferenceException {
//        List<VideoChannelDTO> list = videoConferenceService.selectVideoChannels(1L, null);
//        list.forEach(System.out::println);
        List<VideoChannelDTO> list = videoConferenceService.selectVideoChannels(1L);
        list.forEach(System.out::println);
    }

    @Test
    @DisplayName("화상회의채널 입장 TEST")
    void joinVideoChannel() throws VideoConferenceException {
        videoConferenceService.joinVideoChannel(1L, 2L);

    }

    @Test
    @DisplayName("화상회의채널 삭제 TEST")
    void deleteVideoChannel() throws VideoConferenceException {
        videoConferenceService.deleteVideoChannel(2L);
    }

    @Test
    @DisplayName("화상회의채널 참여자조회 TEST")
    void selectVideoChannelParticipants() throws VideoConferenceException {
        videoConferenceService.selectVideoChannelParticipants(2L).forEach(System.out::println);
    }

    @Test
    @DisplayName("화상회의채널 참여자삭제 TEST")
    void deleteVideoChannelParticipants() throws VideoConferenceException {
        videoConferenceService.deleteVideoChannelParticipant(1L,2L);
    }

    @Test
    void encodeTest() throws VideoConferenceException {
        System.out.println(passwordEncoder.encode("12345678"));
    }

}
**/