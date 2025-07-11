package com.pickteam.repository;

import com.pickteam.domain.videochat.VideoChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VideoChannelRepository extends JpaRepository<VideoChannel, Long> {

    @Query("select v from VideoChannel v where v.workspace.id=?1 and v.isDeleted=false")
    List<VideoChannel> selectChannelsByWorkSpaceId(Long workspaceId);

}
