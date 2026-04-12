package com.portfolio.board.gallery;

public enum GalleryCategory {
    FOOD, CELEBRITY;

    public String getLabel() {
        return switch (this) {
            case FOOD -> "음식";
            case CELEBRITY -> "연예인";
        };
    }
}
