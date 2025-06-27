package com.pickteam.repository.board;

import com.pickteam.domain.board.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {
    Page<Post> findPostsWithCommentsCount(Long boardId, Pageable pageable);
}