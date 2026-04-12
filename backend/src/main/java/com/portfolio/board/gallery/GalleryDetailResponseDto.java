package com.portfolio.board.gallery;

import com.portfolio.attachment.entity.Attachment;
import com.portfolio.attachment.service.AttachmentService;
import com.portfolio.board.common.BoardType;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class GalleryDetailResponseDto {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    private final Long id;
    private final String category;
    private final String categoryLabel;
    private final String title;
    private final String content;
    private final int viewCount;
    private final String createdAt;
    private final String updatedAt;
    private final Long authorId;
    private final String authorName;
    private final boolean isEditable;
    private final List<ImageDto> images;

    public GalleryDetailResponseDto(GalleryPost post, boolean isEditable,
                                     List<Attachment> images, AttachmentService attachmentService) {
        this.id = post.getId();
        this.category = post.getCategory().name();
        this.categoryLabel = post.getCategory().getLabel();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.viewCount = post.getViewCount();
        this.createdAt = post.getCreatedAt().format(FORMATTER);
        this.updatedAt = post.getUpdatedAt().format(FORMATTER);
        this.authorId = post.getUser().getId();
        this.authorName = post.getUser().getName();
        this.isEditable = isEditable;
        this.images = images.stream()
                .map(a -> new ImageDto(a, attachmentService))
                .collect(Collectors.toList());
    }

    @Getter
    public static class ImageDto {
        private final Long id;
        private final String originalName;
        private final String storedName;
        private final String fileUrl;
        private final int sortOrder;

        public ImageDto(Attachment attachment, AttachmentService attachmentService) {
            this.id = attachment.getId();
            this.originalName = attachment.getOriginalName();
            this.storedName = attachment.getStoredName();
            this.fileUrl = attachmentService.buildFileUrl(BoardType.GALLERY, attachment.getStoredName());
            this.sortOrder = attachment.getSortOrder();
        }
    }
}
