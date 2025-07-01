package com.pickteam.repository.board;

import com.pickteam.domain.board.PostAttach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostAttachRepository extends JpaRepository<PostAttach, Long> {

    /**
     * 게시글별 활성 첨부파일 목록을 파일 정보와 함께 조회
     *
     * @param postId 게시글 ID
     * @return 삭제되지 않은 첨부파일 목록
     */
    @Query("SELECT pa FROM PostAttach pa " +
            "JOIN FETCH pa.fileInfo " +
            "WHERE pa.post.id = :postId AND pa.isDeleted = false")
    List<PostAttach> findByPostIdWithFileInfoAndIsDeletedFalse(@Param("postId") Long postId);

    /**
     * 활성 첨부파일 상세 조회
     *
     * @param attachId 첨부파일 ID
     * @return 삭제되지 않은 첨부파일의 상세 정보
     */
    @Query("SELECT pa FROM PostAttach pa " +
            "JOIN FETCH pa.fileInfo " +
            "WHERE pa.id = :attachId AND pa.isDeleted = false")
    Optional<PostAttach> findByIdWithFileInfoAndIsDeletedFalse(@Param("attachId") Long attachId);

    /**
     * ID로 활성 첨부파일 조회
     *
     * @param id 첨부파일 ID
     * @return 삭제되지 않은 첨부파일 (Optional)
     */
    Optional<PostAttach> findByIdAndIsDeletedFalse(Long id);
}