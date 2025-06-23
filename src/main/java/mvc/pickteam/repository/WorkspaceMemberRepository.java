package mvc.pickteam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import mvc.pickteam.entity.WorkspaceMember;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {
    
    Optional<WorkspaceMember> findByWorkspaceIdAndAccountId(Long workspaceId, Long accountId);
    
    List<WorkspaceMember> findByWorkspaceIdAndStatus(Long workspaceId, WorkspaceMember.MemberStatus status);
    
    @Query("SELECT wm FROM WorkspaceMember wm JOIN FETCH wm.account WHERE wm.workspace.id = :workspaceId AND wm.status = 'ACTIVE'")
    List<WorkspaceMember> findActiveMembers(@Param("workspaceId") Long workspaceId);
    
    boolean existsByWorkspaceIdAndAccountId(Long workspaceId, Long accountId);
    
    void deleteByWorkspaceIdAndAccountId(Long workspaceId, Long accountId);
} 