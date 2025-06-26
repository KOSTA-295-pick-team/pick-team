package com.pickteam.dto.board;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class PostAttachCreateDto {
    private MultipartFile file;
}