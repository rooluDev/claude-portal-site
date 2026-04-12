package com.portfolio.board.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardListRequestDto {

    private String startDate;      // YYYY-MM-DD, 기본값: 오늘 - 1년
    private String endDate;        // YYYY-MM-DD, 기본값: 오늘
    private String category;       // ENUM 값 (생략 시 전체)
    private String searchText;     // 최소 2자 미만이면 전체 조회
    private int pageSize = 10;     // 10 / 20 / 30
    private String orderValue = "createdAt"; // createdAt / title / viewCount / category
    private String orderDirection = "desc";  // desc / asc
    private int pageNum = 1;
    private Boolean my = false;    // 문의게시판 전용: true면 본인 글만
}
