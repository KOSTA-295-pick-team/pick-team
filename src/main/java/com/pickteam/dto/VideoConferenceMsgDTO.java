package com.pickteam.dto;

import com.pickteam.util.VideoConferenceControllMsg;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VideoConferenceMsgDTO {
    private VideoConferenceControllMsg type;
    private String userEmail;
    private List<ParticipantDTO> participants;
}
