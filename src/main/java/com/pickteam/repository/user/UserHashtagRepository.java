package com.pickteam.repository.user;

import com.pickteam.domain.user.UserHashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserHashtagRepository extends JpaRepository<UserHashtag, Long> {

    // 해시태그 이름으로 찾기
    Optional<UserHashtag> findByName(String name);

    // 해시태그 검색 (자동완성용)
    @Query("SELECT h FROM UserHashtag h WHERE h.name LIKE %:keyword% ORDER BY h.name")
    List<UserHashtag> findByNameContainingIgnoreCase(@Param("keyword") String keyword);

    // 해시태그 존재 여부 확인
    boolean existsByName(String name);
}
