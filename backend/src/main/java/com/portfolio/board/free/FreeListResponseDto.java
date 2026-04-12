package com.portfolio.board.free;

import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class FreeListResponseDto {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    private final Long id;
    private final String category;
    private final String categoryLabel;
    private final String title;
    private final long commentCount;
    private final boolean hasAttachment;
    private final int viewCount;
    private final String createdAt;
    private final String authorName;

    public FreeListResponseDto(FreePost post, long commentCount, boolean hasAttachment) {
        this.id = post.getId();
        this.category = post.getCategory().name();
        this.categoryLabel = post.getCategory().getLabel();
        this.title = post.getTitle();
        this.commentCount = commentCount;
        this.hasAttachment = hasAttachment;
        this.viewCount = post.getViewCount();
        this.createdAt = post.getCreatedAt().format(FORMATTER);
        this.authorName = post.getUser().getName();
    }
}
