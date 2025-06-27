package com.pickteam.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VideoChannelDTO {

    private Long id;

    @NotBlank(message = "채널 이름은 한글자 이상 입력하여야 합니다")
    private String name;

    private LocalDateTime createdAt;

}

