package com.portfolio.comment.repository;

import com.portfolio.board.common.BoardType;
import com.portfolio.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByBoardTypeAndPostIdOrderByCreatedAtAsc(BoardType boardType, Long postId);

    long countByBoardTypeAndPostId(BoardType boardType, Long postId);

    void deleteByBoardTypeAndPostId(BoardType boardType, Long postId);
}
