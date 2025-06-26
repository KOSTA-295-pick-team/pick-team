package com.pickteam.repository.board;

import com.pickteam.domain.board.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

// PostRepositoryCustom 는 executor 대신에 사용
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.account " +
            "JOIN FETCH p.board " +
            "WHERE p.board.id = :boardId " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findByBoardIdWithAuthor(@Param("boardId") Long boardId, Pageable pageable);

    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.account " +
            "JOIN FETCH p.board " +
            "LEFT JOIN FETCH p.attachments pa " +
            "LEFT JOIN FETCH pa.fileInfo " +
            "WHERE p.id = :postId")
    Optional<Post> findByIdWithDetails(@Param("postId") Long postId);

    @Query("SELECT COALESCE(MAX(p.postNo), 0) FROM Post p WHERE p.board.id = :boardId")
    Integer findMaxPostNoByBoardId(@Param("boardId") Long boardId);
}