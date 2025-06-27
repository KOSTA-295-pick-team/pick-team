package com.pickteam.dto.board;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentCreateDto {

    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;
}
