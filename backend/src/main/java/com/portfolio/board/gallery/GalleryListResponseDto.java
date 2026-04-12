package com.portfolio.board.gallery;

import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class GalleryListResponseDto {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
    private static final int PREVIEW_MAX_LENGTH = 100;

    private final Long id;
    private final String category;
    private final String categoryLabel;
    private final String title;
    private final String contentPreview;
    private final String thumbnailUrl;
    private final int additionalImageCount;
    private final int viewCount;
    private final String createdAt;
    private final String authorName;

    public GalleryListResponseDto(GalleryPost post, String thumbnailUrl, int totalImageCount) {
        this.id = post.getId();
        this.category = post.getCategory().name();
        this.categoryLabel = post.getCategory().getLabel();
        this.title = post.getTitle();
        String raw = post.getContent();
        this.contentPreview = raw.length() > PREVIEW_MAX_LENGTH
                ? raw.substring(0, PREVIEW_MAX_LENGTH) : raw;
        this.thumbnailUrl = thumbnailUrl;
        this.additionalImageCount = Math.max(0, totalImageCount - 1);
        this.viewCount = post.getViewCount();
        this.createdAt = post.getCreatedAt().format(FORMATTER);
        this.authorName = post.getUser().getName();
    }
}
