package com.pickteam.repository.kanban;

import com.pickteam.domain.kanban.KanbanTaskMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KanbanTaskMemberRepository extends JpaRepository<KanbanTaskMember, Long> {
    
    @Query("SELECT ktm FROM KanbanTaskMember ktm WHERE ktm.kanbanTask.id = :kanbanTaskId AND ktm.isDeleted = false")
    List<KanbanTaskMember> findByKanbanTaskId(@Param("kanbanTaskId") Long kanbanTaskId);
    
    @Query("SELECT ktm FROM KanbanTaskMember ktm WHERE ktm.account.id = :accountId AND ktm.isDeleted = false")
    List<KanbanTaskMember> findByAccountId(@Param("accountId") Long accountId);
    
    void deleteByKanbanTaskIdAndAccountId(Long kanbanTaskId, Long accountId);
} 