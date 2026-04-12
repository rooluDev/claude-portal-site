package com.portfolio.board.free;

import com.portfolio.attachment.entity.Attachment;
import com.portfolio.attachment.service.AttachmentService;
import com.portfolio.board.common.BoardType;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class FreeDetailResponseDto {

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
    private final List<AttachmentDto> attachments;

    public FreeDetailResponseDto(FreePost post, boolean isEditable,
                                  List<Attachment> attachments, AttachmentService attachmentService) {
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
        this.attachments = attachments.stream()
                .map(a -> new AttachmentDto(a, attachmentService))
                .collect(Collectors.toList());
    }

    @Getter
    public static class AttachmentDto {
        private final Long id;
        private final String originalName;
        private final String storedName;
        private final long fileSize;
        private final String fileUrl;

        public AttachmentDto(Attachment attachment, AttachmentService attachmentService) {
            this.id = attachment.getId();
            this.originalName = attachment.getOriginalName();
            this.storedName = attachment.getStoredName();
            this.fileSize = attachment.getFileSize();
            this.fileUrl = attachmentService.buildFileUrl(BoardType.FREE, attachment.getStoredName());
        }
    }
}
