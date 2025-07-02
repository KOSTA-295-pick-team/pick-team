package com.pickteam.dto.announcement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공지사항 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnouncementCreateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 255, message = "제목은 255자를 초과할 수 없습니다.")
    private String title;

    private String content; // TEXT 타입으로 변경되어 길이 제한 제거

    @NotNull(message = "팀 ID는 필수입니다.")
    private Long teamId;

    /**
     * 요청 데이터 유효성 검증
     */
    public void validate() {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 필수입니다.");
        }
        if (teamId == null || teamId <= 0) {
            throw new IllegalArgumentException("유효한 팀 ID가 필요합니다.");
        }
    }
}