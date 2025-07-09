package com.pickteam.dto;

import com.fasterxml.jackson.databind.DatabindException;
import lombok.*;

import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantDTO {
    private String identity;
    private String metaData;
    private Date joinedAt;
    private String name;
    private String state;
}
