package com.portfolio.comment.controller;

import com.portfolio.board.common.BoardType;
import com.portfolio.comment.dto.CommentResponseDto;
import com.portfolio.comment.dto.CommentWriteRequestDto;
import com.portfolio.comment.service.CommentService;
import com.portfolio.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponseDto>>> getList(
            @RequestParam BoardType boardType,
            @RequestParam Long postId,
            @AuthenticationPrincipal Long currentUserId,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                commentService.getList(boardType, postId, currentUserId, isAdmin(authentication))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponseDto>> create(
            @RequestBody @Valid CommentWriteRequestDto dto,
            @AuthenticationPrincipal Long currentUserId,
            Authentication authentication) {
        CommentResponseDto result = commentService.create(dto, currentUserId, isAdmin(authentication));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("댓글이 등록되었습니다.", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal Long currentUserId,
            Authentication authentication) {
        commentService.delete(id, currentUserId, isAdmin(authentication));
        return ResponseEntity.ok(ApiResponse.success("댓글이 삭제되었습니다.", null));
    }

    private boolean isAdmin(Authentication auth) {
        if (auth == null) return false;
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
