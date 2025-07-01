package com.pickteam.repository.board;

import com.pickteam.domain.board.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    /**
     * 게시판별 활성 게시글 목록을 작성자 정보와 함께 조회
     *
     * @param boardId 게시판 ID
     * @param pageable 페이징 정보
     * @return 삭제되지 않은 게시글 페이지
     */
    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.account " +
            "JOIN FETCH p.board " +
            "WHERE p.board.id = :boardId AND p.isDeleted = false " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findByBoardIdWithAuthorAndIsDeletedFalse(@Param("boardId") Long boardId, Pageable pageable);

    /**
     * 활성 게시글 상세 조회 (기본 정보만, 컬렉션 제외)
     *
     * @param postId 게시글 ID
     * @return 삭제되지 않은 게시글의 상세 정보
     */
    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.account " +
            "JOIN FETCH p.board " +
            "WHERE p.id = :postId AND p.isDeleted = false")
    Optional<Post> findByIdWithDetailsAndIsDeletedFalse(@Param("postId") Long postId);

    /**
     * ID로 활성 게시글 조회
     *
     * @param id 게시글 ID
     * @return 삭제되지 않은 게시글 (Optional)
     */
    Optional<Post> findByIdAndIsDeletedFalse(Long id);

    /**
     * 게시판별 최대 게시글 번호 조회 (활성 게시글만)
     *
     * @param boardId 게시판 ID
     * @return 해당 게시판의 활성 게시글 중 최대 번호
     */
    @Query("SELECT COALESCE(MAX(p.postNo), 0) FROM Post p WHERE p.board.id = :boardId AND p.isDeleted = false")
    Integer findMaxPostNoByBoardIdAndIsDeletedFalse(@Param("boardId") Long boardId);
}