package com.pickteam.repository.kanban;

import com.pickteam.domain.kanban.KanbanTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KanbanTaskRepository extends JpaRepository<KanbanTask, Long> {
    
    @Query("SELECT kt FROM KanbanTask kt WHERE kt.kanbanList.id = :kanbanListId AND kt.isDeleted = false ORDER BY kt.order ASC")
    List<KanbanTask> findByKanbanListIdOrderByOrder(@Param("kanbanListId") Long kanbanListId);
    
    @Query("SELECT MAX(kt.order) FROM KanbanTask kt WHERE kt.kanbanList.id = :kanbanListId AND kt.isDeleted = false")
    Integer findMaxOrderByKanbanListId(@Param("kanbanListId") Long kanbanListId);
    
    @Query("SELECT kt FROM KanbanTask kt JOIN kt.members ktm WHERE ktm.account.id = :accountId AND kt.isDeleted = false")
    List<KanbanTask> findByAssigneeId(@Param("accountId") Long accountId);
} 