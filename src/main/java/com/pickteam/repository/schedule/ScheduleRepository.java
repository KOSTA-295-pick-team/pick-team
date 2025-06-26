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

    // EAGER 로딩이므로 JOIN FETCH 불필요
    @Query("SELECT s FROM Schedule s " +
            "WHERE s.team.id = :teamId " +
            "ORDER BY s.startDate ASC")
    Page<Schedule> findByTeamIdWithDetails(@Param("teamId") Long teamId, Pageable pageable);

    @Query("SELECT s FROM Schedule s " +
            "WHERE s.id = :scheduleId")
    Optional<Schedule> findByIdWithDetails(@Param("scheduleId") Long scheduleId);

    @Query("SELECT s FROM Schedule s " +
            "WHERE s.team.id = :teamId " +
            "AND s.startDate >= :startDate " +
            "AND s.endDate <= :endDate " +
            "ORDER BY s.startDate ASC")
    List<Schedule> findByTeamIdAndDateRange(
            @Param("teamId") Long teamId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT s FROM Schedule s " +
            "WHERE s.team.id = :teamId " +
            "AND s.type = :type " +
            "ORDER BY s.startDate ASC")
    Page<Schedule> findByTeamIdAndType(
            @Param("teamId") Long teamId,
            @Param("type") ScheduleType type,
            Pageable pageable);

    @Query("SELECT s FROM Schedule s " +
            "WHERE s.account.id = :accountId " +
            "ORDER BY s.startDate ASC")
    Page<Schedule> findByAccountId(@Param("accountId") Long accountId, Pageable pageable);
}