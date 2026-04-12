package com.portfolio.board.notice;

import com.portfolio.board.common.BoardListRequestDto;
import com.portfolio.board.common.PageResponseDto;
import com.portfolio.common.response.ApiResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<NoticeListResponseDto>>> getList(
            @ModelAttribute BoardListRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success(noticeService.getList(dto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NoticeDetailResponseDto>> getDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal Long currentUserId,
            Authentication authentication,
            HttpSession session) {
        boolean isAdmin = isAdmin(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                noticeService.getDetail(id, currentUserId, isAdmin, session)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Long>>> create(
            @RequestBody @Valid NoticeWriteRequestDto dto,
            @AuthenticationPrincipal Long currentUserId) {
        Long id = noticeService.create(dto, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("게시글이 등록되었습니다.", Map.of("id", id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @RequestBody @Valid NoticeWriteRequestDto dto) {
        noticeService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.success("게시글이 수정되었습니다.", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        noticeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("게시글이 삭제되었습니다.", null));
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
