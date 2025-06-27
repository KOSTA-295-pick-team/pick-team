package com.pickteam.domain.user;

import com.pickteam.domain.board.Comment;
import com.pickteam.domain.board.Post;
import com.pickteam.domain.chat.ChatMember;
import com.pickteam.domain.chat.ChatMessage;
import com.pickteam.domain.common.BaseSoftDeleteByAnnotation;
import com.pickteam.domain.common.BaseTimeEntity;
import com.pickteam.domain.enums.UserRole;
import com.pickteam.domain.kanban.KanbanTaskComment;
import com.pickteam.domain.kanban.KanbanTaskMember;
import com.pickteam.domain.notification.NotificationLog;
import com.pickteam.domain.team.TeamMember;
import com.pickteam.domain.videochat.VideoMember;
import com.pickteam.domain.workspace.WorkspaceMember;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 사용자 계정 엔티티
 * - Pick Team 서비스의 핵심 사용자 정보를 관리
 * - 기본 인증 정보와 프로필 정보를 포함
 * - 팀 매칭을 위한 성향 및 작업 스타일 정보 제공
 * - Soft Delete 지원으로 데이터 무결성 보장
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account extends BaseSoftDeleteByAnnotation {

    /** 사용자 고유 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 사용자 이메일 주소 (로그인 ID로 사용, 유니크 제약) */
    @Column(nullable = false, unique = true)
    private String email;

    /** 사용자 비밀번호 (암호화되어 저장) */
    @Column(nullable = false)
    // 실제 저장 시에는 암호화된 값이 저장되어야 함
    private String password;

    /** 사용자 이름 */
    @Column(nullable = false)
    private String name;

    /** 사용자 나이 */
    @Column(nullable = false)
    private Integer age;

    /** 사용자 권한 (ADMIN, USER 등) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    /** MBTI 성격 유형 (팀 매칭 시 참고용, 선택 사항) */
    @Column(length = 4)
    private String mbti;

    /** 사용자 성향/특성 설명 (팀 매칭 시 참고용) */
    @Column(columnDefinition = "TEXT")
    private String disposition;

    /** 사용자 자기소개 */
    @Column(columnDefinition = "TEXT")
    private String introduction;

    /** 포트폴리오 링크 또는 설명 */
    private String portfolio;

    /** 선호하는 작업 스타일 (팀 매칭 알고리즘에 활용) */
    private String preferWorkstyle;

    /** 기피하는 작업 스타일 (팀 매칭 알고리즘에서 제외) */
    private String dislikeWorkstyle;

    // mvc 버전에서 추가된 필드들
    @Column(columnDefinition = "TEXT")
    private String likes;

    @Column(columnDefinition = "TEXT")
    private String dislikes;

    @Column(name = "profile_image")
    private String profileImage;

    // 사용자가 탈퇴해도 관련 정보가 삭제되면 안 된다. cascade 걸지 않고 OnetoMany로 연결만(조회용)
    @OneToMany(mappedBy = "account")
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    /** 사용자가 참여한 채팅방 멤버십 목록 */
    @OneToMany(mappedBy = "account")
    @Builder.Default
    private List<ChatMember> chatMembers = new ArrayList<>();

    /** 사용자가 보낸 채팅 메시지 목록 */
    @OneToMany(mappedBy = "account")
    @Builder.Default
    private List<ChatMessage> chatMessages = new ArrayList<>();

    /** 사용자가 작성한 게시글 목록 */
    @OneToMany(mappedBy = "account")
    @Builder.Default
    private List<Post> posts = new ArrayList<>();

    /** 사용자가 작성한 칸반 태스크 댓글 목록 */
    @OneToMany(mappedBy = "account")
    @Builder.Default
    private List<KanbanTaskComment> kanbanTaskComments = new ArrayList<>();

    /** 사용자가 할당된 칸반 태스크 멤버십 목록 */
    @OneToMany(mappedBy = "account")
    @Builder.Default
    private List<KanbanTaskMember> kanbanTaskMembers = new ArrayList<>();

    /** 사용자에게 발송된 알림 로그 목록 */
    @OneToMany(mappedBy = "account")
    @Builder.Default
    private List<NotificationLog> notificationLogs = new ArrayList<>();

    /** 사용자가 참여한 팀 멤버십 목록 */
    @OneToMany(mappedBy = "account")
    @Builder.Default
    private List<TeamMember> teamMembers = new ArrayList<>();

    /** 사용자가 참여한 화상회의 멤버십 목록 */
    @OneToMany(mappedBy = "account")
    @Builder.Default
    private List<VideoMember> videoMembers = new ArrayList<>();

    /** 사용자가 참여한 워크스페이스 멤버십 목록 */
    @OneToMany(mappedBy = "account")
    @Builder.Default
    private List<WorkspaceMember> workspaceMembers = new ArrayList<>();

    /** 사용자에게 할당된 해시태그 목록 (팀 매칭용) */
    @OneToMany(mappedBy = "account")
    @Builder.Default
    private List<UserHashtagList> userHashtagLists = new ArrayList<>();

}
