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

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account extends BaseSoftDeleteByAnnotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    // 실제 저장 시에는 암호화된 값이 저장되어야 함
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    private String mbti;

    private String disposition;

    private String introduction;

    private String portfolio;

    private String preferWorkstyle;

    private String dislikeWorkstyle;

    // 사용자가 탈퇴해도 관련 정보가 삭제되면 안 된다. cascade 걸지 않고 OnetoMany로 연결만(조회용)
    @OneToMany(mappedBy = "account")
    private List<Comment> comments = new ArrayList<>();
    @OneToMany(mappedBy = "account")
    private List<ChatMember> chatMembers = new ArrayList<>();
    @OneToMany(mappedBy = "account")
    private List<ChatMessage> chatMessages = new ArrayList<>();
    @OneToMany(mappedBy = "account")
    private List<Post> posts = new ArrayList<>();
    @OneToMany(mappedBy = "account")
    private List<KanbanTaskComment> kanbanTaskComments = new ArrayList<>();
    @OneToMany(mappedBy = "account")
    private List<KanbanTaskMember> kanbanTaskMembers = new ArrayList<>();
    @OneToMany(mappedBy = "account")
    private List<NotificationLog> notificationLogs = new ArrayList<>();
    @OneToMany(mappedBy = "account")
    private List<TeamMember> teamMembers = new ArrayList<>();
    @OneToMany(mappedBy = "account")
    private List<VideoMember> videoMembers = new ArrayList<>();
    @OneToMany(mappedBy = "account")
    private List<WorkspaceMember> workspaceMembers = new ArrayList<>();
    @OneToMany(mappedBy = "account")
    private List<UserHashtagList> userHashtagLists = new ArrayList<>();


}
