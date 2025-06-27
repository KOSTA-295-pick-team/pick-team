package com.pickteam.repository.board;

import com.pickteam.domain.board.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.account " +
            "JOIN FETCH p.board " +
            "WHERE p.board.id = :boardId " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findByBoardIdWithAuthor(@Param("boardId") Long boardId, Pageable pageable);

    // 기본 정보만 조회 (컬렉션 제외)
    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.account " +
            "JOIN FETCH p.board " +
            "WHERE p.id = :postId")
    Optional<Post> findByIdWithDetails(@Param("postId") Long postId);

    @Query("SELECT COALESCE(MAX(p.postNo), 0) FROM Post p WHERE p.board.id = :boardId")
    Integer findMaxPostNoByBoardId(@Param("boardId") Long boardId);
}