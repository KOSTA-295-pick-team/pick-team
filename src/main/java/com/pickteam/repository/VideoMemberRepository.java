package com.pickteam.repository;

import com.pickteam.domain.videochat.VideoMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VideoMemberRepository extends JpaRepository<VideoMember, Long> {


    @Query("select vm from VideoMember vm join fetch vm.account where vm.videoChannel.id=?1")
    List<VideoMember> selectAccountsByChannelId(Long channelId);

    @Query("select vm from VideoMember vm where vm.account.id=:accountId and vm.videoChannel.id=:channelId")
    VideoMember findByAccountIdAndVideoChannelId(@Param("accountId") Long accountId, @Param("channelId") Long channelId);

    @Query("select vm from VideoMember vm where vm.videoChannel.id=?1")
    List<VideoMember> selectByVideoChannelId(Long videoChannelId);
}
