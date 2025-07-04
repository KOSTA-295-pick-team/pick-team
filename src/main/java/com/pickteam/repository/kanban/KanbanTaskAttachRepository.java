package com.pickteam.repository.kanban;

import com.pickteam.domain.kanban.KanbanTaskAttach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KanbanTaskAttachRepository extends JpaRepository<KanbanTaskAttach, Long> {
    
    @Query("SELECT kta FROM KanbanTaskAttach kta WHERE kta.kanbanTask.id = :kanbanTaskId AND kta.isDeleted = false")
    List<KanbanTaskAttach> findByKanbanTaskId(@Param("kanbanTaskId") Long kanbanTaskId);
} 