package com.portfolio.board.inquiry;

import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class InquiryListResponseDto {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    private final Long id;
    private final String title;
    private final boolean isSecret;
    private final String answerStatus;
    private final String answerStatusLabel;
    private final int viewCount;
    private final String createdAt;
    private final String authorName;

    public InquiryListResponseDto(Inquiry inquiry) {
        this.id = inquiry.getId();
        this.title = inquiry.isSecret() ? "비밀글입니다." : inquiry.getTitle();
        this.isSecret = inquiry.isSecret();
        this.answerStatus = inquiry.getAnswerStatus().name();
        this.answerStatusLabel = inquiry.getAnswerStatus().getLabel();
        this.viewCount = inquiry.getViewCount();
        this.createdAt = inquiry.getCreatedAt().format(FORMATTER);
        this.authorName = inquiry.getUser().getName();
    }

    // 본인/관리자용: 실제 제목 노출
    public InquiryListResponseDto(Inquiry inquiry, boolean showTitle) {
        this.id = inquiry.getId();
        this.title = showTitle ? inquiry.getTitle() : (inquiry.isSecret() ? "비밀글입니다." : inquiry.getTitle());
        this.isSecret = inquiry.isSecret();
        this.answerStatus = inquiry.getAnswerStatus().name();
        this.answerStatusLabel = inquiry.getAnswerStatus().getLabel();
        this.viewCount = inquiry.getViewCount();
        this.createdAt = inquiry.getCreatedAt().format(FORMATTER);
        this.authorName = inquiry.getUser().getName();
    }
}
