package com.pickteam.dto.kanban;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KanbanTaskCompletionApprovalRequest {
    private String approvalMessage; // 승인 시 메시지 (선택사항)
    private Boolean approved; // true: 승인, false: 거부
}
