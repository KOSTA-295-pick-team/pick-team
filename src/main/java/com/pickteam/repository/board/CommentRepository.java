package com.pickteam.repository.board;

import com.pickteam.domain.board.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.account " +
            "WHERE c.post.id = :postId AND c.isDeleted = false " +
            "ORDER BY c.createdAt ASC")
    Page<Comment> findByPostIdWithAuthor(@Param("postId") Long postId, Pageable pageable);

    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.account " +
            "JOIN FETCH c.post " +
            "WHERE c.id = :commentId AND c.isDeleted = false")
    Optional<Comment> findByIdWithDetails(@Param("commentId") Long commentId);
}