package com.pickteam.repository.kanban;

import com.pickteam.domain.kanban.KanbanTaskComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KanbanTaskCommentRepository extends JpaRepository<KanbanTaskComment, Long> {
    
    @Query("SELECT ktc FROM KanbanTaskComment ktc WHERE ktc.kanbanTask.id = :kanbanTaskId AND ktc.isDeleted = false ORDER BY ktc.createdAt ASC")
    List<KanbanTaskComment> findByKanbanTaskIdOrderByCreatedAt(@Param("kanbanTaskId") Long kanbanTaskId);
    
    @Query("SELECT ktc FROM KanbanTaskComment ktc WHERE ktc.kanbanTask.id = :kanbanTaskId AND ktc.isDeleted = false ORDER BY ktc.createdAt DESC")
    Page<KanbanTaskComment> findByKanbanTaskIdPageable(@Param("kanbanTaskId") Long kanbanTaskId, Pageable pageable);
}