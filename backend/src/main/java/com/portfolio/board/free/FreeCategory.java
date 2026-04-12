package com.portfolio.board.free;

public enum FreeCategory {
    HUMOR, HOBBY;

    public String getLabel() {
        return switch (this) {
            case HUMOR -> "유머";
            case HOBBY -> "취미";
        };
    }
}
