package com.pickteam.repository.board;

import com.pickteam.config.TestQueryDslConfig;
import com.pickteam.domain.board.Board;
import com.pickteam.domain.board.Post;
import com.pickteam.domain.enums.UserRole;
import com.pickteam.domain.team.Team;
import com.pickteam.domain.user.Account;
import com.pickteam.domain.workspace.Workspace;
import com.pickteam.repository.team.TeamRepository;
import com.pickteam.repository.user.AccountRepository;
import com.pickteam.repository.workspace.WorkspaceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * 게시글 리포지토리 테스트
 * @DataJpaTest를 사용하여 JPA Repository 테스트에 집중
 */
@DataJpaTest
@Transactional
@Import(TestQueryDslConfig.class)
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Test
    @DisplayName("삭제되지 않은 게시글을 ID로 조회할 수 있다")
    void findByIdWithDetailsAndIsDeletedFalse_ExistingId_ReturnsPost() {
        // given
        Workspace workspace = createAndSaveWorkspace("테스트 워크스페이스");
        Team team = createAndSaveTeam("개발팀", workspace);
        Board board = createAndSaveBoard(team);
        Account account = createAndSaveAccount("test@example.com", "홍길동");
        Post post = createAndSavePost("게시글 제목", "게시글 내용", account, board);

        // when
        Optional<Post> result = postRepository.findByIdWithDetailsAndIsDeletedFalse(post.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("게시글 제목");
        assertThat(result.get().getContent()).isEqualTo("게시글 내용");
        assertThat(result.get().getAccount().getName()).isEqualTo("홍길동");
        assertThat(result.get().getBoard().getId()).isEqualTo(board.getId());
        assertThat(result.get().getIsDeleted()).isFalse();
    }

    @Test
    @DisplayName("삭제된 게시글은 ID로 조회할 수 없다")
    void findByIdWithDetailsAndIsDeletedFalse_DeletedPost_ReturnsEmpty() {
        // given
        Workspace workspace = createAndSaveWorkspace("테스트 워크스페이스");
        Team team = createAndSaveTeam("개발팀", workspace);
        Board board = createAndSaveBoard(team);
        Account account = createAndSaveAccount("test@example.com", "홍길동");
        Post post = createAndSavePost("게시글 제목", "게시글 내용", account, board);
        
        // 게시글 삭제 처리
        post.markDeleted();
        postRepository.save(post);

        // when
        Optional<Post> result = postRepository.findByIdWithDetailsAndIsDeletedFalse(post.getId());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회하면 빈 결과를 반환한다")
    void findByIdWithDetailsAndIsDeletedFalse_NonExistentId_ReturnsEmpty() {
        // given
        Long nonExistentId = 999L;

        // when
        Optional<Post> result = postRepository.findByIdWithDetailsAndIsDeletedFalse(nonExistentId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("게시판별 게시글을 댓글 수와 함께 페이징 조회할 수 있다")
    void findPostsWithCommentsCount_ValidBoardId_ReturnsPagedPosts() {
        // given
        Workspace workspace = createAndSaveWorkspace("테스트 워크스페이스");
        Team team = createAndSaveTeam("개발팀", workspace);
        Board board = createAndSaveBoard(team);
        Account account = createAndSaveAccount("test@example.com", "홍길동");
        
        // 3개의 게시글 생성
        createAndSavePost("첫 번째 게시글", "첫 번째 내용", account, board);
        createAndSavePost("두 번째 게시글", "두 번째 내용", account, board);
        createAndSavePost("세 번째 게시글", "세 번째 내용", account, board);

        Pageable pageable = PageRequest.of(0, 2); // 첫 번째 페이지, 2개씩

        // when
        Page<Post> result = postRepository.findPostsWithCommentsCount(board.getId(), pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3L);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.hasNext()).isTrue();
        
        // 게시글이 최신순으로 정렬되는지 확인
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("세 번째 게시글");
        assertThat(result.getContent().get(1).getTitle()).isEqualTo("두 번째 게시글");
    }

    @Test
    @DisplayName("게시판에 게시글이 없으면 빈 페이지를 반환한다")
    void findPostsWithCommentsCount_EmptyBoard_ReturnsEmptyPage() {
        // given
        Workspace workspace = createAndSaveWorkspace("테스트 워크스페이스");
        Team team = createAndSaveTeam("개발팀", workspace);
        Board board = createAndSaveBoard(team);

        Pageable pageable = PageRequest.of(0, 5);

        // when
        Page<Post> result = postRepository.findPostsWithCommentsCount(board.getId(), pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0L);
        assertThat(result.getTotalPages()).isEqualTo(0);
    }

    @Test
    @DisplayName("삭제된 게시글은 페이징 조회에 포함되지 않는다")
    void findPostsWithCommentsCount_ExcludesDeletedPosts() {
        // given
        Workspace workspace = createAndSaveWorkspace("테스트 워크스페이스");
        Team team = createAndSaveTeam("개발팀", workspace);
        Board board = createAndSaveBoard(team);
        Account account = createAndSaveAccount("test@example.com", "홍길동");
        
        // 정상 게시글과 삭제된 게시글 생성
        createAndSavePost("정상 게시글", "정상 내용", account, board);
        Post deletedPost = createAndSavePost("삭제될 게시글", "삭제될 내용", account, board);
        deletedPost.markDeleted();
        postRepository.save(deletedPost);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Post> result = postRepository.findPostsWithCommentsCount(board.getId(), pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("정상 게시글");
        assertThat(result.getTotalElements()).isEqualTo(1L);
    }

    // 테스트 헬퍼 메서드들
    private Workspace createAndSaveWorkspace(String name) {
        // 1) Account 생성 및 저장
        Account account = Account.builder()
                .email("unique_" + System.nanoTime() + "@example.com") // 유니크 이메일 권장
                .password("encoded-password")
                .role(UserRole.USER)
                .build();
        account = accountRepository.save(account);  // 저장 후, 식별자(id) 부여된 객체로 교체

        // 2) Workspace 생성 시 account 반드시 넣기
        Workspace workspace = Workspace.builder()
                .name(name)
                .url("url-" + System.nanoTime()) // 유니크 URL
                .account(account)   // null 아님!
                .build();

        return workspaceRepository.save(workspace);
    }

    private Team createAndSaveTeam(String name, Workspace workspace) {
        Team team = Team.builder()
                .name(name)
                .workspace(workspace)
                .build();
        return teamRepository.save(team);
    }

    private Board createAndSaveBoard(Team team) {
        Board board = Board.builder()
                .team(team)
                .build();
        return boardRepository.save(board);
    }

    private Account createAndSaveAccount(String email, String name) {
        Account account = Account.builder()
                .email(email)
                .password("password")
                .name(name)
                .age(25)
                .build();
        return accountRepository.save(account);
    }

    private Post createAndSavePost(String title, String content, Account account, Board board) {
        Post post = Post.builder()
                .title(title)
                .content(content)
                .account(account)
                .board(board)
                .postNo(1)
                .build();
        return postRepository.save(post);
    }
}
