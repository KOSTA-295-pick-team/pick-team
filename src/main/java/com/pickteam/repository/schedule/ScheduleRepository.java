package com.pickteam.repository.schedule;

import com.pickteam.domain.schedule.Schedule;
import com.pickteam.domain.schedule.ScheduleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    /**
     * ID로 활성 일정 조회 (삭제되지 않은 일정만)
     *
     * @param id 일정 ID
     * @return 삭제되지 않은 일정 (Optional)
     */
    Optional<Schedule> findByIdAndIsDeletedFalse(Long id);

    /**
     * 팀별 활성 일정 목록을 상세 정보와 함께 조회
     *
     * @param teamId 팀 ID
     * @param pageable 페이징 정보
     * @return 삭제되지 않은 일정 페이지
     */
    @Query("SELECT s FROM Schedule s " +
            "JOIN FETCH s.account " +
            "LEFT JOIN FETCH s.team " +
            "WHERE s.team.id = :teamId AND s.isDeleted = false " +
            "ORDER BY s.startDate ASC")
    Page<Schedule> findByTeamIdWithDetailsAndIsDeletedFalse(@Param("teamId") Long teamId, Pageable pageable);

    /**
     * 활성 일정 상세 조회
     *
     * @param scheduleId 일정 ID
     * @return 삭제되지 않은 일정의 상세 정보
     */
    @Query("SELECT s FROM Schedule s " +
            "JOIN FETCH s.account " +
            "LEFT JOIN FETCH s.team " +
            "WHERE s.id = :scheduleId AND s.isDeleted = false")
    Optional<Schedule> findByIdWithDetailsAndIsDeletedFalse(@Param("scheduleId") Long scheduleId);

    /**
     * 팀별 기간 내 활성 일정 조회
     *
     * @param teamId 팀 ID
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 해당 기간의 삭제되지 않은 팀 일정 목록
     */
    @Query("SELECT s FROM Schedule s WHERE s.team.id = :teamId " +
            "AND s.isDeleted = false " +
            "AND ((s.startDate BETWEEN :startDate AND :endDate) " +
            "OR (s.endDate BETWEEN :startDate AND :endDate) " +
            "OR (s.startDate <= :startDate AND s.endDate >= :endDate)) " +
            "ORDER BY s.startDate ASC")
    List<Schedule> findByTeamIdAndDateRangeAndIsDeletedFalse(
            @Param("teamId") Long teamId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 팀별 일정 타입별 활성 일정 조회
     *
     * @param teamId 팀 ID
     * @param type 일정 타입
     * @param pageable 페이징 정보
     * @return 해당 타입의 삭제되지 않은 팀 일정 페이지
     */
    @Query("SELECT s FROM Schedule s " +
            "JOIN FETCH s.account " +
            "LEFT JOIN FETCH s.team " +
            "WHERE s.team.id = :teamId AND s.type = :type AND s.isDeleted = false " +
            "ORDER BY s.startDate ASC")
    Page<Schedule> findByTeamIdAndTypeAndIsDeletedFalse(@Param("teamId") Long teamId,
                                                        @Param("type") ScheduleType type,
                                                        Pageable pageable);

    /**
     * 사용자별 활성 일정 조회
     *
     * @param accountId 사용자 ID
     * @param pageable 페이징 정보
     * @return 해당 사용자의 삭제되지 않은 일정 페이지
     */
    @Query("SELECT s FROM Schedule s " +
            "JOIN FETCH s.account " +
            "LEFT JOIN FETCH s.team " +
            "WHERE s.account.id = :accountId AND s.isDeleted = false " +
            "ORDER BY s.startDate ASC")
    Page<Schedule> findByAccountIdAndIsDeletedFalse(@Param("accountId") Long accountId, Pageable pageable);
}