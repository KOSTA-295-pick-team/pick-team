package com.pickteam.dto;

import com.pickteam.domain.common.BaseSoftDeleteSupportEntity;
import com.pickteam.domain.enums.UserRole;
import com.pickteam.domain.user.Account;
import com.pickteam.domain.videochat.VideoChannel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoMemberDTO {

    private Long id;

    private Long userId;

    private String email;

    private String name;

    private String mbti;

    private String disposition;

    private String introduction;

    private String preferWorkstyle;

    private String dislikeWorkstyle;

    private String profileImageUrl;

    private LocalDateTime joinDate;

}
