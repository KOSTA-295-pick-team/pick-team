package com.pickteam.repository.board;

import com.pickteam.domain.board.PostAttach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostAttachRepository extends JpaRepository<PostAttach, Long> {

    @Query("SELECT pa FROM PostAttach pa " +
            "JOIN FETCH pa.fileInfo " +
            "WHERE pa.post.id = :postId")
    List<PostAttach> findByPostIdWithFileInfo(@Param("postId") Long postId);

    @Query("SELECT pa FROM PostAttach pa " +
            "JOIN FETCH pa.fileInfo " +
            "WHERE pa.id = :attachId")
    Optional<PostAttach> findByIdWithFileInfo(@Param("attachId") Long attachId);
}