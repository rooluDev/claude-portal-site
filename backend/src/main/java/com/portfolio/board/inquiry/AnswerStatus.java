package com.portfolio.board.inquiry;

public enum AnswerStatus {
    PENDING, ANSWERED;

    public String getLabel() {
        return switch (this) {
            case PENDING -> "대기중";
            case ANSWERED -> "답변완료";
        };
    }
}
