package com.portfolio.board.inquiry;

import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class InquiryDetailResponseDto {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    private final Long id;
    private final String title;
    private final String content;
    private final boolean isSecret;
    private final String answerStatus;
    private final String answerStatusLabel;
    private final int viewCount;
    private final String createdAt;
    private final String updatedAt;
    private final Long authorId;
    private final String authorName;
    private final boolean isEditable;
    private final AnswerDto answer;

    public InquiryDetailResponseDto(Inquiry inquiry, boolean isEditable, InquiryAnswer answer) {
        this.id = inquiry.getId();
        this.title = inquiry.getTitle();
        this.content = inquiry.getContent();
        this.isSecret = inquiry.isSecret();
        this.answerStatus = inquiry.getAnswerStatus().name();
        this.answerStatusLabel = inquiry.getAnswerStatus().getLabel();
        this.viewCount = inquiry.getViewCount();
        this.createdAt = inquiry.getCreatedAt().format(FORMATTER);
        this.updatedAt = inquiry.getUpdatedAt().format(FORMATTER);
        this.authorId = inquiry.getUser().getId();
        this.authorName = inquiry.getUser().getName();
        this.isEditable = isEditable;
        this.answer = answer != null ? new AnswerDto(answer) : null;
    }

    @Getter
    public static class AnswerDto {
        private final Long id;
        private final String content;
        private final String adminName;
        private final String createdAt;
        private final String updatedAt;

        public AnswerDto(InquiryAnswer answer) {
            this.id = answer.getId();
            this.content = answer.getContent();
            this.adminName = answer.getAdmin().getName();
            this.createdAt = answer.getCreatedAt().format(FORMATTER);
            this.updatedAt = answer.getUpdatedAt().format(FORMATTER);
        }
    }
}
