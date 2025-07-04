package com.pickteam.dto.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSummaryResponse {
    
    private Long id;
    private String name;
    private String email;
    private Integer age;
    private String mbti;
    private String disposition;
    private String introduction;
    private String portfolio;
    private String preferWorkstyle;
    private String dislikeWorkstyle;
    private String profileImageUrl;
    private String role;
} 