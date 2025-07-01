package com.pickteam.repository.board;

import com.pickteam.domain.board.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 게시글별 활성 댓글 목록을 작성자 정보와 함께 조회
     *
     * @param postId 게시글 ID
     * @param pageable 페이징 정보
     * @return 삭제되지 않은 댓글 페이지
     */
    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.account " +
            "WHERE c.post.id = :postId AND c.isDeleted = false " +
            "ORDER BY c.createdAt ASC")
    Page<Comment> findByPostIdWithAuthorAndIsDeletedFalse(@Param("postId") Long postId, Pageable pageable);

    /**
     * 활성 댓글 상세 조회
     *
     * @param commentId 댓글 ID
     * @return 삭제되지 않은 댓글의 상세 정보
     */
    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.account " +
            "JOIN FETCH c.post " +
            "WHERE c.id = :commentId AND c.isDeleted = false")
    Optional<Comment> findByIdWithDetailsAndIsDeletedFalse(@Param("commentId") Long commentId);

    /**
     * ID로 활성 댓글 조회
     *
     * @param id 댓글 ID
     * @return 삭제되지 않은 댓글 (Optional)
     */
    Optional<Comment> findByIdAndIsDeletedFalse(Long id);
}