package com.pickteam.dto;

import lombok.Data;

@Data
public class UserRegisterRequest {

    private String email;
    private String password;
    private String name;
    private String age;
}
