package com.pickteam.dto.user;

import lombok.Data;

@Data
public class UserProfileUpdateRequest {
    private String name;
    private Integer age;
    private String mbti;
    private String disposition;
    private String introduction;
    private String portfolio;
    private String preferWorkstyle;
    private String dislikeWorkstyle;
}
