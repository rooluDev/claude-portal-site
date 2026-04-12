package com.portfolio.comment.dto;

import com.portfolio.comment.entity.Comment;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class CommentResponseDto {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    private final Long id;
    private final Long authorId;
    private final String authorName;
    private final String content;
    private final String createdAt;
    private final boolean isDeletable;

    public CommentResponseDto(Comment comment, boolean isDeletable) {
        this.id = comment.getId();
        this.authorId = comment.getUser().getId();
        this.authorName = comment.getUser().getName();
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt().format(FORMATTER);
        this.isDeletable = isDeletable;
    }
}
