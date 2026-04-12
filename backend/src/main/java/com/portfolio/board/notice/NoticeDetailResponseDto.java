package com.portfolio.board.notice;

import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class NoticeDetailResponseDto {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    private final Long id;
    private final String category;
    private final String categoryLabel;
    private final boolean isPinned;
    private final String title;
    private final String content;
    private final int viewCount;
    private final String createdAt;
    private final String updatedAt;
    private final String authorName;
    private final boolean isEditable;

    public NoticeDetailResponseDto(Notice notice, boolean isEditable) {
        this.id = notice.getId();
        this.category = notice.getCategory().name();
        this.categoryLabel = notice.getCategory().getLabel();
        this.isPinned = notice.isPinned();
        this.title = notice.getTitle();
        this.content = notice.getContent();
        this.viewCount = notice.getViewCount();
        this.createdAt = notice.getCreatedAt().format(FORMATTER);
        this.updatedAt = notice.getUpdatedAt().format(FORMATTER);
        this.authorName = notice.getUser().getName();
        this.isEditable = isEditable;
    }
}
