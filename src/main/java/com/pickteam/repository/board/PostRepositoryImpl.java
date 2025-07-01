package com.pickteam.repository.board;

import com.pickteam.domain.board.Post;
import com.pickteam.domain.board.QPost;
import com.pickteam.domain.board.QComment;
import com.pickteam.domain.user.QAccount;
import com.pickteam.domain.board.QBoard;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Post> findPostsWithCommentsCount(Long boardId, Pageable pageable) {
        QPost post = QPost.post;
        QAccount account = QAccount.account;
        QBoard board = QBoard.board;
        QComment comment = QComment.comment;

        List<Post> content = queryFactory
                .selectFrom(post)
                .join(post.account, account).fetchJoin()
                .join(post.board, board).fetchJoin()
                .leftJoin(post.comments, comment)
                .where(post.board.id.eq(boardId)
                        .and(post.isDeleted.eq(false))) //  Soft Delete 조건 추가
                .groupBy(post.id)
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(Wildcard.count)
                .from(post)
                .where(post.board.id.eq(boardId)
                        .and(post.isDeleted.eq(false))); //  Soft Delete 조건 추가

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}