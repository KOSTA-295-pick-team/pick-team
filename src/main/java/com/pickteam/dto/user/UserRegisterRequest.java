package com.pickteam.dto.user;

import lombok.Data;

@Data
public class UserRegisterRequest {

    private String email;
    private String password;
    private String name;
    private Integer age;
    private String mbti;
    private String disposition;
    private String introduction;
    private String portfolio;
    private String preferWorkstyle;
    private String dislikeWorkstyle;
}
