package com.pickteam.controller;


import com.pickteam.dto.VideoChannelDTO;
import com.pickteam.dto.VideoConferenceMsgDTO;
import com.pickteam.dto.VideoMemberDTO;
import com.pickteam.exception.VideoConferenceException;
import com.pickteam.security.UserPrincipal;
import com.pickteam.service.VideoConferenceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(("/api/workspaces/{workspaceId:\\d+}/video-channels"))
public class VideoConferenceController {

    private final VideoConferenceService videoConferenceService;

    @PreAuthorize("hasRole('ADMIN')  or @videoConferenceAuthService.canViewChannels(#userDetails.id,#workspaceId)")
    @GetMapping
    public ResponseEntity<?> getChannels(@PathVariable Long workspaceId, @AuthenticationPrincipal UserPrincipal userDetails) throws VideoConferenceException {

        List<VideoChannelDTO> videoChannels = videoConferenceService.selectVideoChannels(workspaceId);

        return ResponseEntity.ok(videoChannels);
    }

    @PreAuthorize("hasRole('ADMIN')  or @videoConferenceAuthService.canViewChannels(#userDetails.id,#workspaceId)")
    @GetMapping("/{channelId:\\d+}")
    public ResponseEntity<?> getChannel(@PathVariable Long workspaceId,@PathVariable Long channelId, @AuthenticationPrincipal UserPrincipal userDetails) throws VideoConferenceException {

        VideoChannelDTO videoChannels = videoConferenceService.selectVideoChannel(channelId);

        return ResponseEntity.ok(videoChannels);
    }



    @PreAuthorize("hasRole('ADMIN') or @videoConferenceAuthService.canCreateChannel(#userDetails.id,#workspaceId)")
    @PostMapping
    public ResponseEntity<?> createChannel(@AuthenticationPrincipal UserPrincipal userDetails, @PathVariable Long workspaceId, @Valid @RequestBody VideoChannelDTO videoChannelDTO) throws VideoConferenceException {
        VideoChannelDTO videoChannel = videoConferenceService.insertVideoChannel(workspaceId, videoChannelDTO.getName());
        return ResponseEntity.status(HttpStatus.OK).body(videoChannel);
    }

    @PreAuthorize("hasRole('ADMIN') or  @videoConferenceAuthService.canJoinChannel(#userDetails.id,#workspaceId)")
    @PostMapping("/{channelId}")
    public ResponseEntity<?> joinChannel(@AuthenticationPrincipal UserPrincipal userDetails, @PathVariable("channelId") Long channelId, @PathVariable Long workspaceId) throws VideoConferenceException {


        videoConferenceService.joinVideoChannel(userDetails.getId(), channelId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize("hasRole('ADMIN') or @videoConferenceAuthService.isWorkspaceAdmin(#userDetails.id,#workspaceId)")
    @DeleteMapping("/{channelId:\\d+}")
    public ResponseEntity<?> deleteChannel(@AuthenticationPrincipal UserPrincipal userDetails, @PathVariable("channelId") Long channelId, @PathVariable Long workspaceId) throws VideoConferenceException {

        videoConferenceService.deleteVideoChannel(channelId);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@videoConferenceAuthService.canViewParticipants(#userDetails.id,#channelId)")
    @GetMapping("/{channelId:\\d+}/video-members")
    public ResponseEntity<?> getParticipants(@AuthenticationPrincipal UserPrincipal userDetails, @PathVariable("channelId") Long channelId) throws VideoConferenceException {

        List<VideoMemberDTO> participants = videoConferenceService.selectVideoChannelParticipants(channelId);

        return ResponseEntity.ok(participants);
    }

    @PreAuthorize("hasRole('ADMIN') or @videoConferenceAuthService.isWorkspaceAdmin(#userDetails.id,#workspaceId) or @videoConferenceAuthService.canLeaveChannel(#userDetails.id,#channelId,#memberId)")
    @DeleteMapping("/{channelId:\\d+}/video-members/{memberId}")
    public ResponseEntity<?> leaveChannel(@AuthenticationPrincipal UserPrincipal userDetails, @PathVariable Long memberId, @PathVariable Long channelId, @PathVariable Long workspaceId) throws VideoConferenceException {

        videoConferenceService.deleteVideoChannelParticipant(memberId, channelId);

        return ResponseEntity.noContent().build();
    }


    @PreAuthorize("@videoConferenceAuthService.canJoinConference(#userDetails.id,#channelId)")
    @PostMapping(value = "/{channelId:\\d+}/join-conference")
    public ResponseEntity<Map<String, String>> joinVideoConferenceRoom(@AuthenticationPrincipal UserPrincipal userDetails, @PathVariable Long channelId) throws VideoConferenceException {


        String jwt = videoConferenceService.joinVideoConferenceRoom(userDetails.getId(), channelId, userDetails.getName(), userDetails.getEmail());

        return ResponseEntity.ok(Map.of("token", jwt));
    }

    @PreAuthorize("@videoConferenceAuthService.canCallLiveKitWebhook(#request)")
    @PostMapping(value = "/livekit/webhooks", consumes = "application/webhook+json")
    public ResponseEntity<String> receiveWebhook(HttpServletRequest request, @RequestHeader("Authorization") String authHeader, @RequestBody String body) throws Exception {

        videoConferenceService.handleLiveKitHookEvent(authHeader, body);

        return ResponseEntity.ok("ok");
    }

    @MessageMapping("/video/{roomId}")
    public void handleVideoConferenceControll(@DestinationVariable Long roomId, VideoConferenceMsgDTO msgDTO, Principal principal) throws VideoConferenceException {


        UserPrincipal principalUser = (UserPrincipal) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();

        videoConferenceService.handleVideoConferenceEvent(principalUser.getUsername(), roomId, msgDTO.getType());
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
