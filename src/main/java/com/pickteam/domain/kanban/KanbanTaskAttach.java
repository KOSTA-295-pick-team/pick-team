package com.pickteam.domain.kanban;

import com.pickteam.domain.common.BaseSoftDeleteByAnnotation;
import com.pickteam.domain.common.FileInfo;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanbanTaskAttach extends BaseSoftDeleteByAnnotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private KanbanTask kanbanTask;

    // 첨부파일은 파일 정보와 1:1 매핑된다.
    @OneToOne(cascade = CascadeType.REMOVE, orphanRemoval = true, optional = false)
    @JoinColumn(name = "file_info_id", nullable = false)
    private FileInfo fileInfo;

    @Override
    public void onSoftDelete() {
        super.onSoftDelete();
        if (fileInfo != null) {
            fileInfo.markDeleted(); // soft-delete 전파
        }
    }

}
