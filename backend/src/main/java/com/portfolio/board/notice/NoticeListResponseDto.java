package com.portfolio.board.notice;

import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class NoticeListResponseDto {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    private final Long id;
    private final String category;
    private final String categoryLabel;
    private final boolean isPinned;
    private final String title;
    private final int viewCount;
    private final String createdAt;
    private final String authorName;

    public NoticeListResponseDto(Notice notice) {
        this.id = notice.getId();
        this.category = notice.getCategory().name();
        this.categoryLabel = notice.getCategory().getLabel();
        this.isPinned = notice.isPinned();
        this.title = notice.getTitle();
        this.viewCount = notice.getViewCount();
        this.createdAt = notice.getCreatedAt().format(FORMATTER);
        this.authorName = notice.getUser().getName();
    }
}
