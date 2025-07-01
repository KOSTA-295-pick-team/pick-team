package com.pickteam.repository.user;

import com.pickteam.domain.user.UserHashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserHashtagRepository extends JpaRepository<UserHashtag, Long> {

    // 해시태그 이름으로 찾기 (활성 해시태그만)
    Optional<UserHashtag> findByNameAndIsDeletedFalse(String name);

    // 해시태그 이름으로 찾기 (모든 해시태그, 하위 호환성용)
    Optional<UserHashtag> findByName(String name);

    // 해시태그 검색 (자동완성용) - 성능 최적화: 상위 10개만 조회, soft-delete 필터링 적용
    List<UserHashtag> findTop10ByIsDeletedFalseAndNameContainingIgnoreCaseOrderByName(String name);

    // 기존 메서드는 하위 호환성을 위해 유지하되 soft-delete 필터링 추가
    @Query("SELECT h FROM UserHashtag h WHERE h.isDeleted = false AND UPPER(h.name) LIKE UPPER(CONCAT('%', :keyword, '%')) ORDER BY h.name")
    List<UserHashtag> findByNameContainingIgnoreCase(@Param("keyword") String keyword);

    // 해시태그 존재 여부 확인 (활성 해시태그만)
    boolean existsByNameAndIsDeletedFalse(String name);

    // 해시태그 존재 여부 확인 (모든 해시태그, 하위 호환성용)
    boolean existsByName(String name);
}
