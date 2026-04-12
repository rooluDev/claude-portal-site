package com.portfolio.board.inquiry;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class InquiryAnswerRequestDto {

    @NotBlank(message = "답변 내용을 입력해주세요.")
    private String content;
}
