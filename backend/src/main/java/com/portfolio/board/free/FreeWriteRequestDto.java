package com.portfolio.board.free;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class FreeWriteRequestDto {

    @NotNull(message = "분류를 선택해주세요.")
    private FreeCategory category;

    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 255, message = "제목은 255자를 초과할 수 없습니다.")
    private String title;

    @NotBlank(message = "내용을 입력해주세요.")
    private String content;

    private List<Long> deleteAttachmentIds; // 수정 시 삭제할 첨부파일 ID 목록
}
