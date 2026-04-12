package com.portfolio.board.common;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PageResponseDto<T> {

    private final List<T> content;
    private final long totalCount;
    private final int totalPages;
    private final int currentPage;
    private final int pageSize;

    public PageResponseDto(Page<?> page, List<T> content) {
        this.content = content;
        this.totalCount = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.currentPage = page.getNumber() + 1;
        this.pageSize = page.getSize();
    }
}
