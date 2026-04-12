package com.portfolio.board.notice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class NoticeWriteRequestDto {

    @NotNull(message = "분류를 선택해주세요.")
    private NoticeCategory category;

    @NotNull(message = "입력값이 올바르지 않습니다.")
    private Boolean isPinned;

    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 255, message = "제목은 255자를 초과할 수 없습니다.")
    private String title;

    @NotBlank(message = "내용을 입력해주세요.")
    private String content;
}
