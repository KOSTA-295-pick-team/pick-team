package com.pickteam.controller;


import com.pickteam.dto.VideoChannelDTO;
import com.pickteam.dto.VideoMemberDTO;
import com.pickteam.exception.VideoConferenceException;
import com.pickteam.security.UserPrincipal;
import com.pickteam.service.VideoConferenceService;
import io.livekit.server.WebhookReceiver;
import jakarta.validation.Valid;
import livekit.LivekitWebhook.WebhookEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(("/api/workspaces/{workspaceId:\\d+}/video-channels"))
public class VideoConferenceController {
    @Value("${livekit.api.key:devkey}")
    private String LIVEKIT_API_KEY;

    @Value("${livekit.api.secret:secret}")
    private String LIVEKIT_API_SECRET;


    private final VideoConferenceService videoConferenceService;

    @GetMapping
    public ResponseEntity<?> getChannels(@PathVariable Long workspaceId, @RequestParam(required = false) Long accountId) throws VideoConferenceException {

        List<VideoChannelDTO> videoChannels = videoConferenceService.selectVideoChannels(workspaceId, accountId);

        return ResponseEntity.ok(videoChannels);
    }

    @PostMapping
    public ResponseEntity<?> createChannel(@PathVariable Long workspaceId, @Valid @RequestBody VideoChannelDTO videoChannelDTO) throws VideoConferenceException {
        videoConferenceService.insertVideoChannel(workspaceId, videoChannelDTO.getName());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    /*
    현재 사용자 기능 완성되지 않아 사용불가
     */
//    @PostMapping("/{channelId}")
//    public ResponseEntity<?> joinChannel(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("channelId") Long channelId) {
//
//        //첫번째 인자는 사용자 id 값 전달
//        videoConferenceService.joinVideoChannel(channelId);
//
//        return ResponseEntity.status(HttpStatus.CREATED).build();
//    }

    @DeleteMapping("/{channelId:\\d+}")
    public ResponseEntity<?> deleteChannel(@PathVariable("channelId") Long channelId) throws VideoConferenceException {

        videoConferenceService.deleteVideoChannel(channelId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{channelId:\\d+}/video-members")
    public ResponseEntity<?> getParticipants(@PathVariable("channelId") Long channelId) throws VideoConferenceException {

        List<VideoMemberDTO> participants = videoConferenceService.selectVideoChannelParticipants(channelId);

        return ResponseEntity.ok(participants);
    }

    @DeleteMapping("/{channelId:\\d+}/video-members/{memberId}")
    public ResponseEntity<?> leaveChannel(@PathVariable Long memberId) throws VideoConferenceException {

        videoConferenceService.deleteVideoChannelParticipant(memberId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{channelId:\\d+}/join-conference")
    public ResponseEntity<Map<String, String>> joinVideoConferenceRoom(@AuthenticationPrincipal UserDetails userDetails, @PathVariable  Long channelId) throws VideoConferenceException {

        UserPrincipal userPrincipal = (UserPrincipal) userDetails;

        String jwt = videoConferenceService.joinVideoConferenceRoom(userPrincipal.getId(),channelId,userPrincipal.getUsername());  // accountId는 userDetails에서 user id 뽑아서 넘기기

        return ResponseEntity.ok(Map.of("token", jwt));
    }

    @PostMapping(value = "/livekit/webhooks", consumes = "application/webhook+json")
    public ResponseEntity<String> receiveWebhook(@RequestHeader("Authorization") String authHeader, @RequestBody String body) {
        WebhookReceiver webhookReceiver = new WebhookReceiver(LIVEKIT_API_KEY, LIVEKIT_API_SECRET);
        try {
            WebhookEvent event = webhookReceiver.receive(body, authHeader);
            System.out.println("LiveKit Webhook: " + event.toString());
        } catch (Exception e) {
            System.err.println("Error validating webhook event: " + e.getMessage());
        }
        return ResponseEntity.ok("ok");
    }



    @ExceptionHandler(Exception.class)
    public ProblemDetail handleVideoConferenceException(Exception e) {

        ProblemDetail problemDetail = null;
        if (e instanceof VideoConferenceException ve) {

            log.error(ve.getVideoConferenceErrorCode().getMessage());

            problemDetail = ProblemDetail.forStatus(ve.getVideoConferenceErrorCode().getHttpStatus());

            problemDetail.setTitle(ve.getVideoConferenceErrorCode().getTitle());
            problemDetail.setDetail(ve.getVideoConferenceErrorCode().getMessage());
            problemDetail.setProperty("timestamp", LocalDateTime.now());

        } else if (e instanceof MethodArgumentNotValidException me) {

            log.error(me.getMessage());


            problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

            problemDetail.setTitle("Validation Error");


            List<String> errorMessage = new ArrayList<>();

            me.getBindingResult().getFieldErrors().forEach(fieldError -> {
                errorMessage.add(fieldError.getDefaultMessage());
            });


            problemDetail.setDetail(String.join("\n", errorMessage));
            problemDetail.setProperty("timestamp", LocalDateTime.now());

        } else {

            log.error(e.getMessage());
            problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);

            problemDetail.setTitle("Internal Server Error");
            problemDetail.setDetail("알 수 없는 이유로 오류가 발생하였습니다");
            problemDetail.setProperty("timestamp", LocalDateTime.now());
        }
        return problemDetail;

    }


}
