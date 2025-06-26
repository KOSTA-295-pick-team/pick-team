package com.pickteam.repository.workspace;

import com.pickteam.domain.workspace.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    // URL로 워크스페이스 찾기
    Optional<Workspace> findByUrl(String url);

    // 워크스페이스 이름으로 찾기
    Optional<Workspace> findByName(String name);

    // 사용자가 생성한 워크스페이스 목록 조회
    @Query("SELECT w FROM Workspace w WHERE w.account.id = :accountId")
    List<Workspace> findByAccountId(@Param("accountId") Long accountId);

    // 워크스페이스 이름과 URL 중복 체크
    boolean existsByName(String name);
    boolean existsByUrl(String url);

    // 워크스페이스와 생성자 정보 함께 조회
    @Query("SELECT w FROM Workspace w JOIN FETCH w.account WHERE w.id = :workspaceId")
    Optional<Workspace> findByIdWithAccount(@Param("workspaceId") Long workspaceId);

    // 워크스페이스 이름으로 검색 (부분 일치)
    @Query("SELECT w FROM Workspace w WHERE w.name LIKE %:keyword%")
    List<Workspace> findByNameContaining(@Param("keyword") String keyword);
}