package com.pickteam.dto.board;

import com.pickteam.domain.board.PostAttach;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostAttachResponseDto {

    private Long id;
    private String originalName;
    private String hashedName;
    private Long fileSize;

    public static PostAttachResponseDto from(PostAttach attach) {
        PostAttachResponseDto dto = new PostAttachResponseDto();
        dto.setId(attach.getId());
        dto.setOriginalName(attach.getFileInfo().getNameOrigin());
        dto.setHashedName(attach.getFileInfo().getNameHashed());
        dto.setFileSize(attach.getFileInfo().getSize());
        return dto;
    }
}
