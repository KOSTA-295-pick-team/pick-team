package com.pickteam.dto.board;

import com.pickteam.domain.board.PostAttach;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostAttachResponseDto {
    private Long id;
    private String originalFileName;
    private String hashedFileName;
    private Long fileSize;
    private LocalDateTime createdAt;
    private String downloadUrl;

    public static PostAttachResponseDto from(PostAttach postAttach) {
        return PostAttachResponseDto.builder()
                .id(postAttach.getId())
                .originalFileName(postAttach.getFileInfo().getNameOrigin())
                .hashedFileName(postAttach.getFileInfo().getNameHashed())
                .fileSize(postAttach.getFileInfo().getSize())
                .createdAt(postAttach.getCreatedAt())
                .downloadUrl("/api/files/" + postAttach.getFileInfo().getId() + "/download")
                .build();
    }
}