package com.pickteam.domain.announcement;

import com.pickteam.domain.common.BaseSoftDeleteSupportEntity;
import com.pickteam.domain.user.Account;
import com.pickteam.domain.team.Team;
import jakarta.persistence.*;
import lombok.*;

/**
 * 공지사항 엔티티
 * BaseSoftDeleteSupportEntity를 상속받아 소프트 삭제 기능 제공
 */
@Entity
@Table(name = "announcement")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Announcement extends BaseSoftDeleteSupportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 255)
    private String content;

    // 지연 로딩 적용 - 공지사항 작성자 (Account)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    // 지연 로딩 적용 - 공지사항이 속한 팀
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    /**
     * 공지사항 제목 수정
     * @param title 새로운 제목
     */
    public void updateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 필수입니다.");
        }
        this.title = title;
    }

    /**
     * 공지사항 내용 수정
     * @param content 새로운 내용
     */
    public void updateContent(String content) {
        this.content = content;
    }

    /**
     * 공지사항 수정 (제목과 내용 동시)
     * @param title 새로운 제목
     * @param content 새로운 내용
     */
    public void update(String title, String content) {
        updateTitle(title);
        updateContent(content);
    }

    /**
     * 공지사항 작성자 확인
     * @param accountId 확인할 계정 ID
     * @return 작성자 여부
     */
    public boolean isAuthor(Long accountId) {
        return this.account.getId().equals(accountId);
    }

    /**
     * 작성자 정보 조회 (탈퇴한 사용자 처리 포함)
     * @return 작성자 이름 (탈퇴한 사용자인 경우 "탈퇴한 사용자" 반환)
     */
    public String getAuthorName() {
        if (this.account.isWithdrawnUser()) {
            return "탈퇴한 사용자";
        }
        return this.account.getName();
    }

    /**
     * 작성자가 탈퇴한 사용자인지 확인
     * @return 탈퇴한 사용자면 true
     */
    public boolean isAuthorWithdrawn() {
        return this.account.isWithdrawnUser();
    }
}