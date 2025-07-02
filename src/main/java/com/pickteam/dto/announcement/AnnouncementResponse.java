package com.pickteam.dto.announcement;

import com.pickteam.domain.announcement.Announcement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 공지사항 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnouncementResponse {

    private Long id;
    private String title;
    private String content;
    private Long accountId;
    private String accountName;
    private Long teamId;
    private String teamName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isAuthorWithdrawn; // 작성자 탈퇴 여부

    /**
     * 엔티티를 DTO로 변환하는 정적 팩토리 메서드
     * @param announcement 공지사항 엔티티
     * @return AnnouncementResponse DTO
     */
    public static AnnouncementResponse from(Announcement announcement) {
        return AnnouncementResponse.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .accountId(announcement.getAccount().getId())
                .accountName(announcement.getAuthorName()) // 탈퇴한 사용자 처리 포함
                .teamId(announcement.getTeam().getId())
                .teamName(announcement.getTeam().getName())
                .createdAt(announcement.getCreatedAt())
                .updatedAt(announcement.getUpdatedAt())
                .isAuthorWithdrawn(announcement.isAuthorWithdrawn())
                .build();
    }

    /**
     * 공지사항 미리보기용 DTO 생성 (내용 축약)
     * @param announcement 공지사항 엔티티
     * @param previewLength 미리보기 길이
     * @return 미리보기용 AnnouncementResponse DTO
     */
    public static AnnouncementResponse preview(Announcement announcement, int previewLength) {
        String previewContent = null;
        if (announcement.getContent() != null) {
            previewContent = announcement.getContent().length() > previewLength
                    ? announcement.getContent().substring(0, previewLength) + "..."
                    : announcement.getContent();
        }

        return AnnouncementResponse.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .content(previewContent)
                .accountId(announcement.getAccount().getId())
                .accountName(announcement.getAuthorName())
                .teamId(announcement.getTeam().getId())
                .teamName(announcement.getTeam().getName())
                .createdAt(announcement.getCreatedAt())
                .updatedAt(announcement.getUpdatedAt())
                .isAuthorWithdrawn(announcement.isAuthorWithdrawn())
                .build();
    }
}