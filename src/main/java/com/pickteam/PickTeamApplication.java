package com.pickteam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
//@EnableJpaAuditing
public class PickTeamApplication {

    public static void main(String[] args) {
        SpringApplication.run(PickTeamApplication.class, args);
    }
}