package com.portfolio.attachment.repository;

import com.portfolio.attachment.entity.Attachment;
import com.portfolio.board.common.BoardType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByBoardTypeAndPostIdOrderBySortOrderAsc(BoardType boardType, Long postId);

    Optional<Attachment> findFirstByBoardTypeAndPostIdOrderBySortOrderAsc(BoardType boardType, Long postId);

    boolean existsByBoardTypeAndPostId(BoardType boardType, Long postId);

    long countByBoardTypeAndPostId(BoardType boardType, Long postId);

    void deleteByBoardTypeAndPostId(BoardType boardType, Long postId);
}
