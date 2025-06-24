package com.pickteam.domain.common;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileInfo extends BaseSoftDeleteByAnnotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nameOrigin;

    private String nameHashed;

    private Long size;

    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;

    private Boolean isDeleted;
}
