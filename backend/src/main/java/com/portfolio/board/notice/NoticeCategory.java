package com.portfolio.board.notice;

public enum NoticeCategory {
    NOTICE, EVENT;

    public String getLabel() {
        return switch (this) {
            case NOTICE -> "공지";
            case EVENT -> "이벤트";
        };
    }
}
