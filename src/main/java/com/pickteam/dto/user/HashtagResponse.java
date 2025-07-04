package com.pickteam.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 해시태그 응답 DTO
 * - 사용자 해시태그 조회 시 반환되는 데이터
 * - 해시태그 검색 결과 반환에도 사용
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HashtagResponse {

    /** 해시태그 고유 식별자 */
    private Long id;

    /** 해시태그 이름 */
    private String name;

    /**
     * UserHashtag 엔티티에서 HashtagResponse로 변환
     */
    public static HashtagResponse from(com.pickteam.domain.user.UserHashtag userHashtag) {
        return HashtagResponse.builder()
                .id(userHashtag.getId())
                .name(userHashtag.getName())
                .build();
    }
}
