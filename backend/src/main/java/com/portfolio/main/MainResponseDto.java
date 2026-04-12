package com.portfolio.main;

import com.portfolio.attachment.entity.Attachment;
import com.portfolio.attachment.service.AttachmentService;
import com.portfolio.board.common.BoardType;
import com.portfolio.board.free.FreePost;
import com.portfolio.board.gallery.GalleryPost;
import com.portfolio.board.inquiry.Inquiry;
import com.portfolio.board.notice.Notice;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
public class MainResponseDto {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private final List<NoticeItem> notices;
    private final List<FreeItem> freePosts;
    private final List<GalleryItem> galleryPosts;
    private final List<InquiryItem> inquiries;

    public MainResponseDto(List<NoticeItem> notices, List<FreeItem> freePosts,
                           List<GalleryItem> galleryPosts, List<InquiryItem> inquiries) {
        this.notices = notices;
        this.freePosts = freePosts;
        this.galleryPosts = galleryPosts;
        this.inquiries = inquiries;
    }

    @Getter
    public static class NoticeItem {
        private final Long id;
        private final String category;
        private final String categoryLabel;
        private final String title;
        private final boolean isPinned;
        private final String createdAt;

        public NoticeItem(Notice notice) {
            this.id = notice.getId();
            this.category = notice.getCategory().name();
            this.categoryLabel = notice.getCategory().getLabel();
            this.title = notice.getTitle();
            this.isPinned = notice.isPinned();
            this.createdAt = notice.getCreatedAt().format(FORMATTER);
        }
    }

    @Getter
    public static class FreeItem {
        private final Long id;
        private final String category;
        private final String categoryLabel;
        private final String title;
        private final String authorName;
        private final String createdAt;

        public FreeItem(FreePost post) {
            this.id = post.getId();
            this.category = post.getCategory().name();
            this.categoryLabel = post.getCategory().getLabel();
            this.title = post.getTitle();
            this.authorName = post.getUser().getName();
            this.createdAt = post.getCreatedAt().format(FORMATTER);
        }
    }

    @Getter
    public static class GalleryItem {
        private final Long id;
        private final String title;
        private final String thumbnailUrl;
        private final String authorName;
        private final String createdAt;

        public GalleryItem(GalleryPost post, Attachment thumbnail, AttachmentService attachmentService) {
            this.id = post.getId();
            this.title = post.getTitle();
            this.thumbnailUrl = thumbnail != null
                    ? attachmentService.buildFileUrl(BoardType.GALLERY, thumbnail.getStoredName())
                    : null;
            this.authorName = post.getUser().getName();
            this.createdAt = post.getCreatedAt().format(FORMATTER);
        }
    }

    @Getter
    public static class InquiryItem {
        private final Long id;
        private final String title;
        private final boolean isSecret;
        private final String answerStatus;
        private final String answerStatusLabel;
        private final String createdAt;

        public InquiryItem(Inquiry inquiry) {
            this.id = inquiry.getId();
            this.title = inquiry.isSecret() ? "비밀글입니다." : inquiry.getTitle();
            this.isSecret = inquiry.isSecret();
            this.answerStatus = inquiry.getAnswerStatus().name();
            this.answerStatusLabel = inquiry.getAnswerStatus().getLabel();
            this.createdAt = inquiry.getCreatedAt().format(FORMATTER);
        }
    }
}
