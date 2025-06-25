package com.pickteam.dto.user;

import com.pickteam.domain.enums.UserRole;
import lombok.Data;

@Data
public class UserProfileResponse {
    private Long id;
    private String email;
    private String name;
    private Integer age;
    private UserRole role;
    private String mbti;
    private String disposition;
    private String introduction;
    private String portfolio;
    private String preferWorkstyle;
    private String dislikeWorkstyle;
}
