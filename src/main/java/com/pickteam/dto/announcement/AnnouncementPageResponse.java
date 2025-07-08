package com.pickteam.dto.announcement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 공지사항 페이징 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnouncementPageResponse {

    private List<AnnouncementResponse> announcements; // 공지사항 목록
    private int currentPage;                          // 현재 페이지 번호 (0부터 시작)
    private int totalPages;                           // 전체 페이지 수
    private long totalElements;                       // 전체 게시물 수
    private int size;                                 // 페이지 크기
    private boolean first;                            // 첫 번째 페이지 여부
    private boolean last;                             // 마지막 페이지 여부
    private boolean hasNext;                          // 다음 페이지 존재 여부
    private boolean hasPrevious;                      // 이전 페이지 존재 여부

    /**
     * Page 객체로부터 AnnouncementPageResponse 생성
     *
     * @param page Page<Announcement> 객체
     * @return AnnouncementPageResponse
     */
    public static AnnouncementPageResponse from(Page<AnnouncementResponse> page) {
        return AnnouncementPageResponse.builder()
                .announcements(page.getContent())
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .size(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}
