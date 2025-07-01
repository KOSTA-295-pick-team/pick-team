package com.pickteam.domain.schedule;

import com.pickteam.domain.common.BaseSoftDeleteByAnnotation;
import com.pickteam.domain.common.BaseSoftDeleteSupportEntity;
import com.pickteam.domain.team.Team;
import com.pickteam.domain.user.Account;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule extends BaseSoftDeleteSupportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String title;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Column(length = 100)
    private String scheduleDesc;

    @Enumerated(EnumType.STRING)
    private ScheduleType type;

    /**
     * 일정 생성자 (사용자)
     * - LAZY 로딩으로 성능 최적화 (수동 Soft Delete 방식에서 지원)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Account account;

    /**
     * 소속 팀 정보 (팀 일정인 경우)
     * - LAZY 로딩으로 성능 최적화
     * - 개인 일정인 경우 null 가능
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;
}