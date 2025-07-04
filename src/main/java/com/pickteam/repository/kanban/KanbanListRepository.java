package com.pickteam.repository.kanban;

import com.pickteam.domain.kanban.KanbanList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KanbanListRepository extends JpaRepository<KanbanList, Long> {
    
    @Query("SELECT kl FROM KanbanList kl WHERE kl.kanban.id = :kanbanId AND kl.isDeleted = false ORDER BY kl.order ASC")
    List<KanbanList> findByKanbanIdOrderByOrder(@Param("kanbanId") Long kanbanId);
    
    @Query("SELECT MAX(kl.order) FROM KanbanList kl WHERE kl.kanban.id = :kanbanId AND kl.isDeleted = false")
    Integer findMaxOrderByKanbanId(@Param("kanbanId") Long kanbanId);
} 