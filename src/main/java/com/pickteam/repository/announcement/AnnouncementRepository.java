package com.pickteam.repository.announcement;

import com.pickteam.domain.announcement.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 공지사항 레포지토리
 * SoftDelete 수동 관리를 위한 별도 쿼리 메서드 제공
 */
@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    /**
     * 삭제되지 않은 공지사항을 ID로 조회 (Account, Team 정보 함께 조회)
     * @param id 공지사항 ID
     * @return 공지사항 Optional
     */
    @Query("SELECT a FROM Announcement a " +
            "JOIN FETCH a.account acc " +
            "JOIN FETCH a.team t " +
            "WHERE a.id = :id AND a.isDeleted = false")
    Optional<Announcement> findByIdAndIsDeletedFalse(@Param("id") Long id);

    /**
     * 워크스페이스의 삭제되지 않은 모든 공지사항 조회 (계정, 팀 정보 함께 조회)
     * @param workspaceId 워크스페이스 ID
     * @return 공지사항 리스트
     */
    @Query("SELECT a FROM Announcement a " +
            "JOIN FETCH a.account acc " +
            "JOIN FETCH a.team t " +
            "WHERE t.workspace.id = :workspaceId " +
            "AND a.isDeleted = false " +
            "ORDER BY a.createdAt DESC")
    List<Announcement> findByWorkspaceIdAndIsDeletedFalse(@Param("workspaceId") Long workspaceId);

    /**
     * 팀의 삭제되지 않은 모든 공지사항 조회 (계정 정보 함께 조회)
     * @param teamId 팀 ID
     * @return 공지사항 리스트
     */
    @Query("SELECT a FROM Announcement a " +
            "JOIN FETCH a.account acc " +
            "JOIN FETCH a.team t " +
            "WHERE a.team.id = :teamId " +
            "AND a.isDeleted = false " +
            "ORDER BY a.createdAt DESC")
    List<Announcement> findByTeamIdAndIsDeletedFalse(@Param("teamId") Long teamId);

    /**
     * 계정이 작성한 삭제되지 않은 공지사항 조회
     * @param accountId 계정 ID
     * @return 공지사항 리스트
     */
    @Query("SELECT a FROM Announcement a " +
            "JOIN FETCH a.team t " +
            "WHERE a.account.id = :accountId " +
            "AND a.isDeleted = false " +
            "ORDER BY a.createdAt DESC")
    List<Announcement> findByAccountIdAndIsDeletedFalseOrderByCreatedAtDesc(@Param("accountId") Long accountId);

    /**
     * 워크스페이스 내 삭제되지 않은 공지사항 개수 조회
     * @param workspaceId 워크스페이스 ID
     * @return 공지사항 개수
     */
    @Query("SELECT COUNT(a) FROM Announcement a " +
            "WHERE a.team.workspace.id = :workspaceId " +
            "AND a.isDeleted = false")
    long countByWorkspaceIdAndIsDeletedFalse(@Param("workspaceId") Long workspaceId);

    /**
     * 특정 팀의 삭제되지 않은 공지사항 개수 조회
     * @param teamId 팀 ID
     * @return 공지사항 개수
     */
    long countByTeamIdAndIsDeletedFalse(Long teamId);

    /**
     * 제목 또는 내용으로 공지사항 검색 (워크스페이스 내)
     * @param workspaceId 워크스페이스 ID
     * @param keyword 검색 키워드
     * @return 공지사항 리스트
     */
    @Query("SELECT a FROM Announcement a " +
            "JOIN FETCH a.account acc " +
            "JOIN FETCH a.team t " +
            "WHERE t.workspace.id = :workspaceId " +
            "AND (a.title LIKE %:keyword% OR a.content LIKE %:keyword%) " +
            "AND a.isDeleted = false " +
            "ORDER BY a.createdAt DESC")
    List<Announcement> findByWorkspaceIdAndKeywordAndIsDeletedFalse(@Param("workspaceId") Long workspaceId,
                                                                    @Param("keyword") String keyword);

    /**
     * 탈퇴하지 않은 계정이 작성한 공지사항만 조회
     * @param workspaceId 워크스페이스 ID
     * @return 공지사항 리스트
     */
    @Query("SELECT a FROM Announcement a " +
            "JOIN FETCH a.account acc " +
            "JOIN FETCH a.team t " +
            "WHERE t.workspace.id = :workspaceId " +
            "AND a.isDeleted = false " +
            "AND acc.email IS NOT NULL " +
            "ORDER BY a.createdAt DESC")
    List<Announcement> findByWorkspaceIdAndAccountNotWithdrawnAndIsDeletedFalse(@Param("workspaceId") Long workspaceId);

    /**
     * 특정 계정이 작성한 공지사항 존재 여부 확인
     * @param accountId 계정 ID
     * @return 공지사항이 존재하면 true
     */
    boolean existsByAccountIdAndIsDeletedFalse(Long accountId);
}