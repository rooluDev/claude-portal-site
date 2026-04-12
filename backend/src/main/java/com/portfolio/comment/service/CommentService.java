package com.portfolio.comment.service;

import com.portfolio.board.common.BoardType;
import com.portfolio.comment.dto.CommentResponseDto;
import com.portfolio.comment.dto.CommentWriteRequestDto;
import com.portfolio.comment.entity.Comment;
import com.portfolio.comment.repository.CommentRepository;
import com.portfolio.common.exception.CustomException;
import com.portfolio.common.exception.ErrorCode;
import com.portfolio.user.entity.User;
import com.portfolio.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CommentResponseDto> getList(BoardType boardType, Long postId,
                                            Long currentUserId, boolean isAdmin) {
        return commentRepository.findByBoardTypeAndPostIdOrderByCreatedAtAsc(boardType, postId)
                .stream()
                .map(c -> {
                    boolean isDeletable = currentUserId != null &&
                            (c.getUser().getId().equals(currentUserId) || isAdmin);
                    return new CommentResponseDto(c, isDeletable);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponseDto create(CommentWriteRequestDto dto, Long currentUserId, boolean isAdmin) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));

        Comment comment = Comment.builder()
                .user(user)
                .boardType(dto.getBoardType())
                .postId(dto.getPostId())
                .content(dto.getContent())
                .build();
        commentRepository.save(comment);

        boolean isDeletable = true; // 방금 작성했으므로 본인
        return new CommentResponseDto(comment, isDeletable);
    }

    @Transactional
    public void delete(Long id, Long currentUserId, boolean isAdmin) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getUser().getId().equals(currentUserId) && !isAdmin) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        commentRepository.delete(comment);
    }
}
