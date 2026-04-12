package com.portfolio.comment.dto;

import com.portfolio.board.common.BoardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CommentWriteRequestDto {

    @NotNull(message = "게시판 타입을 입력해주세요.")
    private BoardType boardType;

    @NotNull(message = "게시글 ID를 입력해주세요.")
    private Long postId;

    @NotBlank(message = "댓글 내용을 입력해주세요.")
    @Size(max = 1000, message = "댓글은 1000자를 초과할 수 없습니다.")
    private String content;
}
