package com.pickteam.dto.board;

import com.pickteam.domain.board.Post;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@Setter
public class PostResponseDto {

    private Long id;
    private Integer postNo;
    private String title;
    private String content;
    private String authorName;
    private Long authorId;
    private Long boardId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PostAttachResponseDto> attachments;
    private int commentCount;

    public static PostResponseDto from(Post post) {
        PostResponseDto dto = new PostResponseDto();
        dto.setId(post.getId());
        dto.setPostNo(post.getPostNo());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setAuthorName(post.getAccount().getName());
        dto.setAuthorId(post.getAccount().getId());
        dto.setBoardId(post.getBoard().getId());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        // Optional과 Stream을 활용한 null 안전 처리
        dto.setAttachments(Optional.ofNullable(post.getAttachments())
                .orElse(Collections.emptyList())
                .stream()
                .map(PostAttachResponseDto::from)
                .collect(Collectors.toList()));

        dto.setCommentCount(Optional.ofNullable(post.getComments())
                .map(List::size)
                .orElse(0));

        return dto;
    }
}
